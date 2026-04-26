package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * One per each client.
 * Handles incoming messages, parsing the JSON, the routing calls, & sending responses.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
