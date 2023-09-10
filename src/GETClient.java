import java.net.*;
import java.io.*;

public class GETClient {
    private String serverUrl;

    public GETClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void requestData() {
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide the server URL as an argument.");
            return;
        }

        GETClient client = new GETClient(args[0]);
        client.requestData();
    }
}
