package com.tictactoe.server;

import static com.tictactoe.server.message.GameOverMessageBean.Result.TIED;
import static com.tictactoe.server.message.GameOverMessageBean.Result.YOU_WIN;
import static com.tictactoe.server.message.TurnMessageBean.Turn.WAITING;
import static com.tictactoe.server.message.TurnMessageBean.Turn.YOUR_TURN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import com.google.gson.Gson;
import com.tictactoe.game.Game;
import com.tictactoe.game.Game.PlayerLetter;
import com.tictactoe.game.Player;
import com.tictactoe.server.message.GameOverMessageBean;
import com.tictactoe.server.message.HandshakeMessageBean;
import com.tictactoe.server.message.IncomingMessageBean;
import com.tictactoe.server.message.OutgoingMessageBean;
import com.tictactoe.server.message.TurnMessageBean;
import org.jboss.netty.handler.codec.http.websocketx.*;

/**
 * Handles a server-side channel for a multiplayer game of Tic Tac Toe.
 * 
 * 
 */
public class TicTacToeServerHandler extends SimpleChannelUpstreamHandler {

	static Map<Integer, Game> games = new HashMap<Integer, Game>();

	private static final String WEBSOCKET_PATH = "/websocket";
        
        private WebSocketServerHandshaker handshaker;

	/* (non-Javadoc)
	 * 
	 * An incoming message (event). Invoked when either a:
	 * 
	 * - A player navigates to the page. The initial page load triggers an HttpRequest. We perform the WebSocket handshake 
	 * 		and assign them to a particular game.
	 * 
	 * - OR A player clicks on a tic tac toe square. The message contains who clicked 
	 * 		on which square (1 thru 9) and which game they're playing.
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	/**
	 * Handles all HttpRequests. Must be a GET. Performs the WebSocket handshake 
	 * and assigns a player to a game.
	 * 
	 * @param ctx
	 * @param req
	 * @throws Exception
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req)
			throws Exception {

		// Allow only GET methods.
		if (req.getMethod() != HttpMethod.GET) {
			sendHttpResponse(ctx, req, new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}

          // Handshake
          WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                  this.getWebSocketLocation(req), null, false);
          this.handshaker = wsFactory.newHandshaker(req);
          if (this.handshaker == null) {
              wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
         } else {
              
              this.handshaker.handshake(ctx.getChannel(), req);
              initGame(ctx);
         }
	}
	
	/**
	 * Initializes a game. Finds an open game for a player (if another player is already waiting) or creates a new game.
	 * 
	 * @param ctx
	 */
	private void initGame(ChannelHandlerContext ctx) {
		// Try to find a game waiting for a player. If one doesn't exist, create a new one.
		Game game = findGame();
		
		// Create a new instance of player and assign their channel for WebSocket communications.
		Player player = new Player(ctx.getChannel());
		
		// Add the player to the game.
		Game.PlayerLetter letter = game.addPlayer(player);
		
		// Add the game to the collection of games.
		games.put(game.getId(), game);
		
		// Send confirmation message to player with game ID and their assigned letter (X or O) 
		ctx.getChannel().write(new TextWebSocketFrame(new HandshakeMessageBean(game.getId(), letter.toString()).toJson()));
		
		// If the game has begun we need to inform the players. Send them a "turn" message (either "waiting" or "your_turn")
		if (game.getStatus() == Game.Status.IN_PROGRESS) {			
			game.getPlayer(PlayerLetter.X).getChannel().write(new TextWebSocketFrame(new TurnMessageBean(YOUR_TURN).toJson()));
			game.getPlayer(PlayerLetter.O).getChannel().write(new TextWebSocketFrame(new TurnMessageBean(WAITING).toJson()));
		}
	}
	
	/**
	 * Finds an open game for a player (if another player is waiting) or creates a new game.
	 *
	 * @return Game
	 */
	private Game findGame() {		
		// Find an existing game and return it
		for (Game g : games.values()) {
			if (g.getStatus().equals(Game.Status.WAITING)) {
				return g;
			}
                }		
		// Or return a new game
		return new Game();
	}

	/**
	 * Process turn data from players. Message contains which square they clicked on. Sends turn data to their
	 * opponent.
	 * 
	 * @param ctx
	 * @param frame
	 */
	private void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame) {
            
                   // Check for closing frame
         if (frame instanceof CloseWebSocketFrame) {
             this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
             return;
         } else if (frame instanceof PingWebSocketFrame) {
             ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
             return;
         } else if (!(frame instanceof TextWebSocketFrame)) {
             throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                     .getName()));
         }
		
		Gson gson = new Gson();
		IncomingMessageBean message = gson.fromJson(((TextWebSocketFrame) frame).getText(), IncomingMessageBean.class);
		
		Game game = games.get(message.getGameId());
		Player opponent = game.getOpponent(message.getPlayer());
		Player player = game.getPlayer(PlayerLetter.valueOf(message.getPlayer()));
		
		// Mark the cell the player selected.
		game.markCell(message.getGridIdAsInt(), player.getLetter());
		
		// Get the status for the current game.
		boolean winner = game.isPlayerWinner(player.getLetter());
		boolean tied = game.isTied();
		
		// Respond to the opponent in order to update their screen.
		String responseToOpponent = new OutgoingMessageBean(player.getLetter().toString(), message.getGridId(), winner, tied).toJson();		
		opponent.getChannel().write(new TextWebSocketFrame(responseToOpponent));
		
		// Respond to the player to let them know they won.
		if (winner) {
			player.getChannel().write(new TextWebSocketFrame(new GameOverMessageBean(YOU_WIN).toJson()));
		} else if (tied) {
			player.getChannel().write(new TextWebSocketFrame(new GameOverMessageBean(TIED).toJson()));
		}
	}

	private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req,
			HttpResponse res) {
		// Generate an error page if response status code is not OK (200).
		if (res.getStatus().getCode() != 200) {
			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus()
					.toString(), CharsetUtil.UTF_8));
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.getChannel().write(res);
		if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
	}
}