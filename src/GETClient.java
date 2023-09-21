import java.io.*;
import java.net.*;
import org.json.JSONObject;

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
            JSONObject jsonObject = new JSONObject(jsonString);

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
        System.out.println("id:" + jsonObj.getString("id"));
        System.out.println("name:" + jsonObj.getString("name"));
        System.out.println("state:" + jsonObj.getString("state"));
        System.out.println("time_zone:" + jsonObj.getString("time_zone"));
        System.out.println("lat:" + jsonObj.getDouble("lat"));
        System.out.println("lon:" + jsonObj.getDouble("lon"));
        System.out.println(
                "local_date_time:" + getShortDateTime(String.valueOf(jsonObj.getLong("local_date_time_full"))));
        System.out.println("local_date_time_full:" + jsonObj.getLong("local_date_time_full"));
        System.out.println("air_temp:" + jsonObj.getDouble("air_temp"));
        System.out.println("apparent_t:" + jsonObj.getDouble("apparent_t"));
        System.out.println("cloud:" + jsonObj.getString("cloud"));
        System.out.println("dewpt:" + jsonObj.getDouble("dewpt"));
        System.out.println("press:" + jsonObj.getDouble("press"));
        System.out.println("rel_hum:" + jsonObj.getInt("rel_hum"));
        System.out.println("wind_dir:" + jsonObj.getString("wind_dir"));
        System.out.println("wind_spd_kmh:" + jsonObj.getDouble("wind_spd_kmh"));
        System.out.println("wind_spd_kt:" + jsonObj.getDouble("wind_spd_kt"));
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
