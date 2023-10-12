import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;

public class ContentServer {

    private static final int INTERVAL = 60000; // 60 seconds
    private static LamportClock clock = new LamportClock(); // Create a persistent LamportClock instance

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please provide the port number for the Aggregation Server.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
            return;
        }

        String[] filenames = {
                "src/weather_data.txt",
                "src/weather_data2.txt"
        };

        Modify modify = new Modify();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                modify.processFiles(filenames);
                sendDataToServer(filenames, port);
            }
        }, 0, INTERVAL);
    }

    private static void sendDataToServer(String[] filenames, int port) {
        for (String filename : filenames) {
            try {
                // Increment the Lamport clock for the send action
                clock.sendAction();

                // Read and parse the file into a JSON string
                String jsonData = parseFileToJSON(filename);

                // Send this data to the Aggregation Server using sockets
                Socket s = new Socket("localhost", port);

                // Construct the HTTP PUT request string
                String httpPutRequest = "PUT /weather.json HTTP/1.1\r\n" +
                        "User-Agent: ContentServer/1.0\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: " + jsonData.length() + "\r\n" +
                        "LamportClock: " + clock.getTime() + "\r\n\r\n" +
                        jsonData;

                DataOutputStream dout = new DataOutputStream(s.getOutputStream());

                // Send the HTTP PUT request string
                dout.writeUTF(httpPutRequest);

                dout.flush();
                dout.close();
                s.close();

            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String parseFileToJSON(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Map<String, Object> dataMap = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                // Check if value is a number
                try {
                    double doubleValue = Double.parseDouble(value);
                    dataMap.put(key, doubleValue);
                } catch (NumberFormatException e) {
                    dataMap.put(key, value);
                }
            }
        }
        reader.close();

        return new JSONObject(dataMap).toString();
    }
}
