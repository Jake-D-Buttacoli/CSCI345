package client;

import java.io.*;
import java.net.Socket;
import com.google.gson.Gson;
import shared.Message;

public class Client {

    private Socket clientSocket;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private final Gson gson = new Gson();

    public interface MessageListener {
        void onMessageReceived(Message msg);
    }

    public boolean connectAndLogin(String username, String password) throws IOException {
        clientSocket = new Socket("localhost", 6789);

        inFromServer = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
        );

        outToServer = new DataOutputStream(clientSocket.getOutputStream());

        Message loginMsg = new Message();
        loginMsg.type = "login";
        loginMsg.username = username;
        loginMsg.password = password;

        outToServer.writeBytes(gson.toJson(loginMsg) + "\n");

        String responseLine = inFromServer.readLine();
        Message response = gson.fromJson(responseLine, Message.class);

        return response != null && "login_success".equals(response.type);
    }

    public void startListening(MessageListener listener) {
        new Thread(() -> {
            try {
                String line;

                while ((line = inFromServer.readLine()) != null) {
                    Message msg = gson.fromJson(line, Message.class);
                    listener.onMessageReceived(msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean sendMessage(String toUser, String content) {
        try {
            Message msg = new Message();
            msg.type = "message";
            msg.to = toUser;
            msg.content = content;

            outToServer.writeBytes(gson.toJson(msg) + "\n");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() throws IOException {
        if (outToServer != null) {
            outToServer.writeBytes("{\"type\":\"disconnect\"}\n");
        }

        if (clientSocket != null) {
            clientSocket.close();
        }
    }
}