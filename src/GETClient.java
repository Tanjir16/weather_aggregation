import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.Scanner;

public class GETClient extends WebSocketClient {

    public GETClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server");
        // send a message upon connection
        this.send("Hello from client!");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        URI uri = URI.create("ws://localhost:8080");
        GETClient client = new GETClient(uri);
        client.connect();

        // Allow some time for the connection to establish.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start a new thread to continuously read user input and send it.
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter a message to send (type 'exit' to quit):");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    client.close();
                    System.exit(0);
                }
                client.send(input);
            }
        }).start();
    }
}
