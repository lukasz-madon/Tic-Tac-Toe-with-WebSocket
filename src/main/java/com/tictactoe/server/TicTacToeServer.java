package com.tictactoe.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/* * A HTTP server which serves Web Socket requests at:
 * 
  * http://localhost:9000/websocket
  * 
  * Open your browser at http://localhost:8080/, then the demo page will be loaded and a Web Socket connection will be
  * made automatically.
  * 
  * This server illustrates support for the different web socket specification versions and will work with:
  * 
  * <ul>
  * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
  * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
  * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
  * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
  * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
  * </ul>
  */
public class TicTacToeServer {

    public static void main(String[] args) throws Exception {
        ChannelFactory factory =
            new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.bind(new InetSocketAddress(9000));
        
        System.out.println("TicTacToe Server: Listening on port 9000");
    }
}