import java.io.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GETClient {

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 4567);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            LamportClock clock = new LamportClock();
            clock.sendAction(); // increment the clock for sending action

            // Send a GET request with LamportClock
            String httpRequest = "GET /weather.json HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "LamportClock: " + clock.getTime() + "\r\n\r\n";
            dout.writeUTF(httpRequest);
            dout.flush();

            // Receive the response
            DataInputStream dis = new DataInputStream(s.getInputStream());
            String responseData = dis.readUTF();
            System.out.println("Response from Server: \n" + responseData);

            // Extract JSON data from the response
            int jsonStartIndex = responseData.indexOf("{");
            String jsonString = responseData.substring(jsonStartIndex);

            // Create a JSONObject
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);

            // Display the data
            printFormattedData(jsonObject);

            dout.close();
            dis.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void printFormattedData(JSONObject jsonObj) {
        System.out.println("id:" + (String) jsonObj.get("id"));
        System.out.println("name:" + (String) jsonObj.get("name"));
        System.out.println("state:" + (String) jsonObj.get("state"));
        System.out.println("time_zone:" + (String) jsonObj.get("time_zone"));
        System.out.println("lat:" + (double) jsonObj.get("lat"));
        System.out.println("lon:" + (double) jsonObj.get("lon"));
        System.out.println(
                "local_date_time:" + getShortDateTime(String.valueOf((long) jsonObj.get("local_date_time_full"))));
        System.out.println("local_date_time_full:" + (long) jsonObj.get("local_date_time_full"));
        System.out.println("air_temp:" + (double) jsonObj.get("air_temp"));
        System.out.println("apparent_t:" + (double) jsonObj.get("apparent_t"));
        System.out.println("cloud:" + (String) jsonObj.get("cloud"));
        System.out.println("dewpt:" + (double) jsonObj.get("dewpt"));
        System.out.println("press:" + (double) jsonObj.get("press"));
        System.out.println("rel_hum:" + (int) (long) jsonObj.get("rel_hum"));
        System.out.println("wind_dir:" + (String) jsonObj.get("wind_dir"));
        System.out.println("wind_spd_kmh:" + (double) jsonObj.get("wind_spd_kmh"));
        System.out.println("wind_spd_kt:" + (double) jsonObj.get("wind_spd_kt"));
    }

    private static String getShortDateTime(String fullDateTime) {
        String day = fullDateTime.substring(6, 8);
        String month = fullDateTime.substring(4, 6);
        String hour = fullDateTime.substring(8, 10);
        String minute = fullDateTime.substring(10, 12);
        return day + "/" + month + ":" + hour + minute + "pm";
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
