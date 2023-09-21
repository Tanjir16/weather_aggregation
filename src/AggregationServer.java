import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

public class AggregationServer {

    private static final int DEFAULT_PORT = 4567;
    private static LamportClock globalClock = new LamportClock();
    private static Map<String, JSONObject> contentData = new ConcurrentHashMap<>();
    private static Map<String, Long> lastReceivedTimestamp = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + DEFAULT_PORT);
            }
        }

        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    removeOldContentServers();
                }
            }, 0, 5000); // Check every 5 seconds

            while (true) {
                Socket s = ss.accept();
                DataInputStream dis = new DataInputStream(s.getInputStream());
                String receivedRequest = dis.readUTF();
                System.out.println("Received request: " + receivedRequest);

                if (receivedRequest.startsWith("GET")) {
                    handleGET(receivedRequest, s);
                } else if (receivedRequest.startsWith("PUT")) {
                    handlePUT(receivedRequest, s);
                } else {
                    sendResponse(s, 400, "Bad Request");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleGET(String request, Socket s) throws IOException {
        System.out.println("Received a GET request.");
        JSONObject aggregatedData = new JSONObject(contentData);
        sendResponse(s, 200, aggregatedData.toString());
    }

    private static void handlePUT(String request, Socket s) throws IOException {
        System.out.println("Received a PUT request.");
        String[] headers = request.split("\r\n");
        String clockValue = null;
        for (String header : headers) {
            if (header.startsWith("LamportClock:")) {
                clockValue = header.split(":")[1].trim();
                globalClock.receiveAction(Integer.parseInt(clockValue));
            }
        }

        if (clockValue == null) {
            sendResponse(s, 400, "Bad Request: Missing LamportClock");
            return;
        }

        String jsonData = headers[headers.length - 1];
        JSONObject data = new JSONObject(jsonData);

        contentData.put(clockValue, data);
        lastReceivedTimestamp.put(clockValue, System.currentTimeMillis());

        if (contentData.size() == 1) { // If it's the first time data has been added
            sendResponse(s, 201, "Created");
        } else {
            sendResponse(s, 200, "OK");
        }
    }

    private static void removeOldContentServers() {
        long currentTime = System.currentTimeMillis();
        lastReceivedTimestamp.entrySet().removeIf(entry -> currentTime - entry.getValue() > 30 * 1000);
    }

    private static void sendResponse(Socket s, int statusCode, String message) throws IOException {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        dos.writeUTF("HTTP/1.1 " + statusCode + " " + message + "\r\n\r\n");
        dos.flush();
        dos.close();
        s.close();
    }

    static class LamportClock {
        private int timestamp;

        public LamportClock() {
            this.timestamp = 0;
        }

        public synchronized int getTime() {
            return this.timestamp;
        }

        public synchronized void sendAction() {
            this.timestamp += 1;
        }

        public synchronized void receiveAction(int sourceTimestamp) {
            this.timestamp = Math.max(this.timestamp, sourceTimestamp) + 1;
        }
    }
}
