import java.io.*;
import java.net.*;
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

        System.out.println("Queue size after adding: " + putQueue.size());

        processPutQueue();

        System.out.println("Queue size after processing: " + putQueue.size());

        sendResponse(s, 200, "OK");

    }

    private static synchronized void processPutQueue() {
        System.out.println("Starting processing of the queue.");
        PutRequest request;
        while ((request = putQueue.poll()) != null) {
            System.out.println("Processing request with timestamp: " + request.getLamportTimestamp());

            globalClock.receiveAction(request.getLamportTimestamp());

            JSONObject data = request.getData();
            printFormattedData(data);

            contentData.put(String.valueOf(request.getLamportTimestamp()), data);
            mostRecentData = data;
            lastReceivedTimestamp.put(String.valueOf(request.getLamportTimestamp()), System.currentTimeMillis());

        }
        System.out.println("Finished processing of the queue.");
    }

    private static void printFormattedData(JSONObject jsonObj) {
        String formattedData = "id: " + jsonObj.getString("id") + "\n" +
                "name: " + jsonObj.getString("name") + "\n" +
                "state: " + jsonObj.getString("state") + "\n" +
                "time_zone: " + jsonObj.getString("time_zone") + "\n" +
                "lat: " + jsonObj.getDouble("lat") + "\n" +
                "lon: " + jsonObj.getDouble("lon") + "\n" +
                "local_date_time: " + getShortDateTime(String.valueOf(jsonObj.getLong("local_date_time_full"))) + "\n" +
                "local_date_time_full: " + jsonObj.getLong("local_date_time_full") + "\n" +
                "air_temp: " + jsonObj.getDouble("air_temp") + "\n" +
                "apparent_t: " + jsonObj.getDouble("apparent_t") + "\n" +
                "cloud: " + jsonObj.getString("cloud") + "\n" +
                "dewpt: " + jsonObj.getDouble("dewpt") + "\n" +
                "press: " + jsonObj.getDouble("press") + "\n" +
                "rel_hum: " + jsonObj.getInt("rel_hum") + "\n" +
                "wind_dir: " + jsonObj.getString("wind_dir") + "\n" +
                "wind_spd_kmh: " + jsonObj.getDouble("wind_spd_kmh") + "\n" +
                "wind_spd_kt: " + jsonObj.getDouble("wind_spd_kt") + "\n";

        System.out.println(formattedData);
    }

    private static String getShortDateTime(String fullDateTime) {
        if (fullDateTime.length() == 14) { // Ensure the format is as expected, like '20230715160000'
            String day = fullDateTime.substring(6, 8);
            String month = fullDateTime.substring(4, 6);
            String hour = fullDateTime.substring(8, 10);
            String minute = fullDateTime.substring(10, 12);
            return day + "/" + month + ":" + hour + minute + "pm"; // Assumes all times are PM.
        }
        return fullDateTime; // Return original if not as expected
    }

    private static void removeOldContentServers() {
        long currentTime = System.currentTimeMillis();
        Set<String> staleKeys = lastReceivedTimestamp.entrySet().stream()
                .filter(entry -> currentTime - entry.getValue() > 30 * 1000)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        lastReceivedTimestamp.keySet().removeAll(staleKeys);
        contentData.keySet().removeAll(staleKeys);

        System.out.println("Remaining contentData keys: " + contentData.keySet());
        System.out.println("Remaining lastReceivedTimestamp keys: " + lastReceivedTimestamp.keySet());

        if (mostRecentData != null && !contentData.values().contains(mostRecentData)) {
            mostRecentData = null;
        }
    }

    private static String getKeyForMostRecentData() {
        for (Map.Entry<String, JSONObject> entry : contentData.entrySet()) {
            if (entry.getValue().equals(mostRecentData)) {
                return entry.getKey();
            }
        }
        return null;
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
