import java.net.*;
import java.io.*;

public class AggregationServer {
    private static final int PORT = 8080;
    private DataStore dataStore;

    public AggregationServer() {
        this.dataStore = new DataStore();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new RequestHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RequestHandler implements Runnable {
        private Socket socket;

        public RequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String request = in.readLine();
                if (request != null && request.startsWith("GET")) {
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 13\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "\r\n" +
                            "Hello, Tanjir!";
                    out.write(response);
                    out.flush();
                }

                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AggregationServer().start();
    }
}
