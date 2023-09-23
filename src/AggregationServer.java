import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import java.util.stream.Collectors;

public class AggregationServer {

    private static final int DEFAULT_PORT = 4567;
    private static LamportClock globalClock = new LamportClock();
    private static JSONObject mostRecentData = null;
    private static Map<String, JSONObject> contentData = new ConcurrentHashMap<>();
    private static Map<String, Long> lastReceivedTimestamp = new ConcurrentHashMap<>();
    private static PriorityQueue<PutRequest> putQueue = new PriorityQueue<>(
            Comparator.comparingInt(PutRequest::getLamportTimestamp));
    private static final String BACKUP_FILE = "backup.json";
    private static ServerSocket ss = null;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + DEFAULT_PORT);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown command received. Attempting graceful shutdown...");
            writeToBackupFile();
            if (ss != null && !ss.isClosed()) {
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        try {
            ss = new ServerSocket(port);
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

                if (receivedRequest.startsWith("GET")) {
                    handleGET(receivedRequest, s);
                } else if (receivedRequest.startsWith("PUT")) {
                    handlePUT(receivedRequest, s);
                } else {
                    sendResponse(s, 400, "Bad Request");
                }
            }
        } catch (SocketException e) {
            System.out.println("Server socket closed. Performing cleanup operations.");
            writeToBackupFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleGET(String request, Socket s) throws IOException {
        System.out.println("Received a GET request.");
        JSONObject aggregatedData = mostRecentData != null ? mostRecentData : new JSONObject();
        System.out.println("Returning data to client: " + aggregatedData.toString());
        sendResponse(s, 200, aggregatedData.toString());
    }

    private static synchronized void handlePUT(String request, Socket s) throws IOException {
        System.out.println("Received a PUT request.");
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

        String jsonData = headers[headers.length - 1];
        JSONObject data = new JSONObject(jsonData);

        putQueue.add(new PutRequest(Integer.parseInt(clockValue), data));

        processPutQueue();

        sendResponse(s, 200, "OK");
    }

    private static synchronized void processPutQueue() {
        PutRequest request;
        while ((request = putQueue.poll()) != null) {
            globalClock.receiveAction(request.getLamportTimestamp());

            JSONObject data = request.getData();
            contentData.put(String.valueOf(request.getLamportTimestamp()), data);
            mostRecentData = data;
            lastReceivedTimestamp.put(String.valueOf(request.getLamportTimestamp()), System.currentTimeMillis());
        }
    }

    private static void removeOldContentServers() {
        long currentTime = System.currentTimeMillis();
        Set<String> staleKeys = lastReceivedTimestamp.entrySet().stream()
                .filter(entry -> currentTime - entry.getValue() > 30 * 1000)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        lastReceivedTimestamp.keySet().removeAll(staleKeys);
        contentData.keySet().removeAll(staleKeys);

        if (mostRecentData != null && !contentData.values().contains(mostRecentData)) {
            mostRecentData = null;
        }
    }

    private static void sendResponse(Socket s, int statusCode, String message) throws IOException {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        dos.writeUTF("HTTP/1.1 " + statusCode + " " + message + "\r\n\r\n");
        dos.flush();
        dos.close();
        s.close();
    }

    private static void writeToBackupFile() {
        try (FileWriter file = new FileWriter(BACKUP_FILE)) {
            if (mostRecentData != null) {
                file.write(mostRecentData.toString());
                System.out.println("Successfully copied JSON object to " + BACKUP_FILE);
            } else {
                System.out.println("Most recent data is null. Backup not created.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PutRequest {
        private final int lamportTimestamp;
        private final JSONObject data;

        public PutRequest(int lamportTimestamp, JSONObject data) {
            this.lamportTimestamp = lamportTimestamp;
            this.data = data;
        }

        public int getLamportTimestamp() {
            return lamportTimestamp;
        }

        public JSONObject getData() {
            return data;
        }
    }
}
