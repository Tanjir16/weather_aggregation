import java.io.*;
import java.net.*;

public class GETClient {

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 6666);

            // Create and initialize the LamportClock
            LamportClock clock = new LamportClock();
            clock.sendAction(); // this increments the clock for sending action

            // Construct the HTTP request string
            String httpRequest = "GET /path/resource.ext HTTP/1.1\r\n" +
                    "Host: localhost\r\n" + // Set host to localhost
                    "LamportClock: " + clock.getTime() + "\r\n\r\n"; // Use the clock's timestamp here

            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // Send the HTTP request string
            dout.writeUTF(httpRequest);

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
