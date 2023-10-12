import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class GETClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java GETClient <port> <ID>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
            return;
        }

        String id = args[1];
        LamportClock clock = new LamportClock();
        clock.sendAction(); // increment the clock for sending action

        try {
            Socket s = new Socket("localhost", port);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String httpRequest = "GET /weather.json?id=" + id + " HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "LamportClock: " + clock.getTime() + "\r\n\r\n";

            dout.writeUTF(httpRequest);
            dout.flush();

            DataInputStream dis = new DataInputStream(s.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line;
            StringBuilder responseData = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseData.append(line).append("\n");
            }

            System.out.println("Response from Server: \n" + responseData.toString());

            int jsonStartIndex = responseData.indexOf("{");
            if (jsonStartIndex == -1) {
                System.out.println("Invalid response from the server.");
                return;
            }

            String jsonString = responseData.substring(jsonStartIndex);
            JSONObject jsonObject = new JSONObject(jsonString);

            printFormattedData(jsonObject);

            dout.close();
            dis.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void printFormattedData(JSONObject jsonObj) {
        jsonObj.keySet().forEach(key -> {
            Object value = jsonObj.get(key);
            System.out.println(key + ": " + value);
        });
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
