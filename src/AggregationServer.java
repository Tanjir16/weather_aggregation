import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class AggregationServer extends WebSocketServer {

    public AggregationServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Handle incoming messages here
        System.out.println("Received message: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        AggregationServer server = new AggregationServer(new InetSocketAddress("localhost", 8080));
        server.start();
        System.out.println("WebSocket server started on port 8080");
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server Started");
    }

}
