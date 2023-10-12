import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class AggregationServer {

    private static final int DEFAULT_HTTP_PORT = 5000;
    private static final int DEFAULT_SOCKET_PORT = 5001;
    private static LamportClock globalClock = new LamportClock();
    private static JSONObject mostRecentData = null;
    private static Map<Integer, Long> lastReceivedTimestamp = new ConcurrentHashMap<>();
    private static ServerSocket ss = null;
    private static Map<String, JSONObject> contentData = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int httpPort = DEFAULT_HTTP_PORT;
        int socketPort = DEFAULT_SOCKET_PORT;

        if (args.length > 0) {
            try {
                httpPort = Integer.parseInt(args[0]);
                socketPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_SOCKET_PORT;
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default ports " + DEFAULT_HTTP_PORT + " and "
                        + DEFAULT_SOCKET_PORT);
            }
        }

        System.out.println("Initializing server...");

        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
            httpServer.createContext("/", new RequestHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            System.out.println("HTTP server started on port " + httpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown command received. Attempting graceful shutdown...");
            try {
                if (ss != null && !ss.isClosed()) {
                    ss.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            ss = new ServerSocket(socketPort);
            System.out.println("Socket server started on port " + socketPort);

            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    removeOldContentServers();
                }
            }, 0, 5000);

            while (true) {
                System.out.println("Waiting for client connection...");
                Socket s = ss.accept();
                System.out.println("Client connected: " + s.getInetAddress());
                DataInputStream dis = new DataInputStream(s.getInputStream());
                String receivedRequest = dis.readUTF();

                if (receivedRequest.startsWith("GET")) {
                    handleGET(receivedRequest, s);
                } else if (receivedRequest.startsWith("PUT")) {
                    handlePUT(receivedRequest, s);
                } else {
                    sendResponse(s, 400, "Bad Request");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("Exception occurred: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String query = httpExchange.getRequestURI().getQuery();
            String id = null;
            if (query != null && query.startsWith("id=")) {
                id = query.split("=")[1];
            }

            JSONObject aggregatedData = AggregationServer.filterDataById(id); // Corrected this line

            String response = aggregatedData.toString();
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static JSONObject filterDataById(String ids) {
        if (ids == null || ids.isEmpty()) {
            return new JSONObject(contentData); // Return all data if no ID is specified
        }

        JSONObject filteredData = new JSONObject();
        String[] idArray = ids.split(",");

        for (String id : idArray) {
            System.out.println("Looking for ID: " + id);
            JSONObject data = contentData.get(id); // Get data by ID
            if (data != null) {
                System.out.println("Found data for ID " + id + ": " + data);
                filteredData.put(id, data);
            }
        }

        return filteredData;
    }

    // Following methods handle incoming GET and PUT requests, managing data and
    // responding to clients
    private static void handleGET(String request, Socket s) throws IOException {
        System.out.println("Received a GET request.");
        String id = null;
        if (request.contains("?id=")) {
            id = request.split("\\?id=")[1].split(" ")[0];
        }

        JSONObject aggregatedData = filterDataById(id);
        System.out.println("Returning data to client: " + aggregatedData.toString());
        sendResponse(s, 200, aggregatedData.toString());
    }

    // Similar to above, with additional handling for PUT requests and managing data
    // storage and updates
    private static synchronized void handlePUT(String request, Socket s) throws IOException {
        System.out.println("Received a PUT request: " + request);
        String[] headers = request.split("\r\n");
        String clockValue = null;

        for (String header : headers) {
            if (header.startsWith("LamportClock:")) {
                clockValue = header.split(":")[1].trim();
            }
        }

        if (clockValue == null) {
            sendResponse(s, 400, "Bad Request: Missing LamportClock");
            return;
        }

        int lamportTimestamp = Integer.parseInt(clockValue);
        globalClock.receiveAction(lamportTimestamp);

        String jsonData = headers[headers.length - 1];
        JSONObject data = new JSONObject(jsonData);

        // Add the LamportClock value to the JSONObject
        data.put("LamportClock", lamportTimestamp);

        // Extract ID from the JSON data
        String id = data.has("id") ? data.getString("id") : "unknown"; // Make sure to handle this appropriately

        if (mostRecentData == null || lamportTimestamp > mostRecentData.getInt("LamportClock")) {
            mostRecentData = data;
            System.out.println("Updated mostRecentData with Lamport Timestamp: " + lamportTimestamp);
        } else {
            System.out.println("Received older data. No update needed.");
        }

        // Store all received data if needed, not just the most recent
        contentData.put(id, data);
        lastReceivedTimestamp.put(lamportTimestamp, System.currentTimeMillis());

        System.out.println(
                "Processed PUT request. Lamport Timestamp: " + lamportTimestamp + ", Data: " + data.toString());
        printContentAndTimestampKeys();

        sendResponse(s, 200, "OK");

        System.out.println("Current mostRecentData: " + mostRecentData);
    }

    // The remaining code is similar to that above, ensuring data is managed,
    // updated, and stale data is removed as needed
    private static void removeOldContentServers() {
        long currentTime = System.currentTimeMillis();
        Set<String> staleIds = lastReceivedTimestamp.entrySet().stream()
                .filter(entry -> currentTime - entry.getValue() > 30 * 1000) // 30 seconds expiry time
                .map(entry -> contentData.entrySet().stream()
                        .filter(dataEntry -> dataEntry.getValue().has("LamportClock") &&
                                dataEntry.getValue().getInt("LamportClock") == entry.getKey())
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        staleIds.forEach(id -> {
            lastReceivedTimestamp.remove(id);
            contentData.remove(id);
        });

        printContentAndTimestampKeys();
    }

    private static void sendResponse(Socket s, int statusCode, String message) throws IOException {
        OutputStream os = s.getOutputStream();
        String httpResponse = "HTTP/1.1 " + statusCode + " " + message + "\r\n\r\n";
        os.write(httpResponse.getBytes());

        // If there is a JSON message body, add it here
        if (statusCode == 200 && message != null && !message.equals("OK")) {
            os.write(message.getBytes());
        }

        os.flush();
        os.close();
        s.close();
    }

    private static void printContentAndTimestampKeys() {
        System.out.println("contentData keys: " + contentData.keySet());
        System.out.println("lastReceivedTimestamp keys: " + lastReceivedTimestamp.keySet());
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
