import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class ContentServer {

    public static void main(String[] args) {
        try {
            // 1. Initialize LamportClock and perform a send action.
            LamportClock clock = new LamportClock();
            clock.sendAction();

            // 2. Read and parse the file into a JSON string
            String jsonData = parseFileToJSON("weather_data.txt"); // command line multiple file /// 30 sec pause

            // 3. Send this data to the Aggregation Server using sockets
            Socket s = new Socket("localhost", 6666);

            // 4. Construct the HTTP PUT request string
            String httpPutRequest = "PUT /weather.json HTTP/1.1\r\n" +
                    "User-Agent: ATOMClient/1/0\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + jsonData.length() + "\r\n" +
                    "LamportClock: " + clock.getTime() + "\r\n\r\n" +
                    jsonData;

            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // 5. Send the HTTP PUT request string
            dout.writeUTF(httpPutRequest);

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String parseFileToJSON(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
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
