import org.json.JSONObject;
import java.io.*;
import java.net.*;

public class GETClient {

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 4567);

            // Create and initialize the LamportClock
            LamportClock clock = new LamportClock();
            clock.sendAction(); // this increments the clock for sending action

            // Construct the HTTP request string
            String httpRequest = "GET /weather.json HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "LamportClock: " + clock.getTime() + "\r\n\r\n";

            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // Send the HTTP request string
            dout.writeUTF(httpRequest);
            dout.flush();

            // Receive the data from AggregationServer
            DataInputStream dis = new DataInputStream(s.getInputStream());
            String responseData = dis.readUTF();

            // Split the response by newline
            String[] responseLines = responseData.split("\r\n");

            // Identify the start of the JSON by iterating over the split response
            StringBuilder jsonBuilder = new StringBuilder();
            boolean jsonStarted = false;
            for (String line : responseLines) {
                if (line.startsWith("{")) {
                    jsonStarted = true;
                }
                if (jsonStarted) {
                    jsonBuilder.append(line);
                } else {
                    jsonBuilder.setLength(0); // Clear the jsonBuilder if the line doesn't start with a '{' and we
                                              // haven't started capturing the JSON yet
                }
            }
            // Locate the start of the JSON data
            int jsonStartIndex = responseData.indexOf("{");
            if (jsonStartIndex == -1) {
                System.out.println("JSON data not found in the response. The raw response is: " + responseData);
                return;
            }

            String jsonPart = responseData.substring(jsonStartIndex);

            // Convert the JSON string to JSONObject
            JSONObject jsonObject = new JSONObject(jsonPart);
            JSONObject weatherData = jsonObject.getJSONObject(jsonObject.keys().next());

            // Format and print the data
            printFormattedData(weatherData);

            dout.close();
            dis.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
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
        if (fullDateTime.length() == 14) {
            String day = fullDateTime.substring(6, 8);
            String month = fullDateTime.substring(4, 6);
            String hour = fullDateTime.substring(8, 10);
            String minute = fullDateTime.substring(10, 12);
            return day + "/" + month + ":" + hour + minute + "pm";
        }
        return fullDateTime;
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
