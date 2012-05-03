// Constants - Status Updates
var STRATEGIZING_STATUS = "Your opponent's turn.";
var WAITING_STATUS = "Waiting for an opponent.";
var YOUR_TURN_STATUS = "It's your turn!";
var YOU_WIN_STATUS = "WINNER!";
var TIED_STATUS = "The game is tied.";
var WEBSOCKET_CLOSED_STATUS = "Connection has been closed.";

// Constants - Game
var PLAYER_O = "O";
var PLAYER_X = "X";

// Constants - Incoming message types
var MESSAGE_HANDSHAKE = "handshake";
var MESSAGE_OPPONENT_UPDATE = "response";
var MESSAGE_TURN_INDICATOR = "turn";
var MESSAGE_GAME_OVER = "game_over";

// Constants - Message turn indicator types
var MESSAGE_TURN_INDICATOR_YOUR_TURN = "YOUR_TURN";
var MESSAGE_TURN_INDICATOR_WAITING = "WAITING";

// Constants - Game over message types
var MESSAGE_GAME_OVER_YOU_WIN = "YOU_WIN";
var MESSAGE_GAME_OVER_TIED = "TIED";

// Constants - WebSocket URL
var WEBSOCKET_URL = "ws://localhost:9000/websocket";

// Variables
var player;
var opponent;
var gameId;
var yourTurn = false;

// WebSocket connection
var ws;

$(document).ready(function() {
    
  if (typeof MozWebSocket != "undefined") { // (window.MozWebSocket)
     ws = new MozWebSocket(WEBSOCKET_URL);
  } else if (window.WebSocket) {
     ws = new WebSocket(WEBSOCKET_URL);
  } else {
    alert('ERROR: This browser does not support sWebSockets');
  }	
	// bind to .grid class 
	$(".grid").click(function () {
            // Only process clicks if it's your turn.
            if (yourTurn == true) { 
	    // Stop processing clicks and invoke sendMessage(). 
            yourTurn = false;
            sendMessage(this.id);
    	    // Add the X or O to the game board and update status.
	      $("#" + this.id).addClass(player);
	      $("#" + this.id).html(player);	    	  
	      $('#status').text(STRATEGIZING_STATUS);    	 					      
    	}
    });	
    
    ws.onopen = function(event) { 
    	$('#status').text(WAITING_STATUS); 
        //alert('onopen');
    }
    
    // Process turn message ("push") from the server.
    ws.onmessage = function(event) {
                // alert('onmessage');
            var message = jQuery.parseJSON(event.data);
            // Process the handshake response when the page is opened
            if (message.type === MESSAGE_HANDSHAKE) {
                gameId = message.gameId;
                player = message.player;

                if (player === PLAYER_X) {
                        opponent = PLAYER_O; 
                } else {
                        opponent = PLAYER_X;   	 	 	
                }
            }
            // Process your opponent's turn data.
            if (message.type === MESSAGE_OPPONENT_UPDATE) {
                // Show their turn info on the game board.
                $("#" + message.gridId).addClass(message.opponent);
                $("#" + message.gridId).html(message.opponent);
                // Switch to your turn.
                if (message.winner == true) {
                        $('#status').text(message.opponent + " is the winner!"); 
                } else if (message.tied == true) {
                        $('#status').text(TIED_STATUS);   	   	 			
                } else {
                        yourTurn = true;
                $('#status').text(YOUR_TURN_STATUS);    	   	 			
                }
            }   	 	
            /* The initial turn indicator from the server. Determines who starts
                the game first. Both players wait until the server gives the OK
                to start a game. */
            if (message.type === MESSAGE_TURN_INDICATOR) {
                    if (message.turn === MESSAGE_TURN_INDICATOR_YOUR_TURN) {
                            yourTurn = true;
                    $('#status').text(YOUR_TURN_STATUS);    	 			
                } else if (message.turn === MESSAGE_TURN_INDICATOR_WAITING) {
                            $('#status').text(STRATEGIZING_STATUS);    	 					    	
                }
            }

            /* The server has determined you are the winner and sent you this message. */
            if (message.type === MESSAGE_GAME_OVER) {
                    if (message.result === MESSAGE_GAME_OVER_YOU_WIN) {
                            $('#status').text(YOU_WIN_STATUS);
                    } 
                    else if (message.result === MESSAGE_GAME_OVER_TIED) {
                            $('#status').text(TIED_STATUS);
                    }
            }	
    } 

    ws.onclose = function(event) { 
                //alert('close');
            $('#status').text(WEBSOCKET_CLOSED_STATUS); 
    } 		
});

// Send your turn information to the server.
function sendMessage(id) {
	var message = {gameId: gameId, player: player, gridId:id};
	var encoded = $.toJSON(message);
	ws.send(encoded);
}
