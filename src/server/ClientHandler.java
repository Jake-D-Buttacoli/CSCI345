package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.google.gson.Gson;
import shared.Message;

/**
 * One per each client.
 * Handles incoming messages, parsing the JSON, routing calls, and sending responses.
 */
public class ClientHandler implements Runnable {

    private final Gson gson = new Gson();

    private final Socket socket;
    private DataOutputStream out;
    private String username;
    private boolean authenticated;
    private boolean connected = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            out = new DataOutputStream(socket.getOutputStream());

            String message;
            while (connected && (message = in.readLine()) != null) {
                handleMessage(message);
            }

        } catch (Exception e) {
            if (!connected) {
                return;
            }

            if (e instanceof IOException
                    && e.getMessage() != null
                    && e.getMessage().contains("Broken pipe")) {
                cleanup();
                return;
            }

            e.printStackTrace();

        } finally {
            cleanup();
        }
    }

    private void handleMessage(String jsonString) throws IOException {
        Message msg = gson.fromJson(jsonString, Message.class);

        if (msg == null || msg.type == null) {
            sendJson(error("Invalid message format"));
            return;
        }

        switch (msg.type) {

            case "login":
                handleLogin(msg);
                break;

            case "message":
                if (!authenticated) {
                    sendJson(error("Please login first"));
                    return;
                }

                MessageRouter.sendMessageToUser(username, msg.to, msg.content);
                break;

            case "broadcast":
                if (!authenticated) {
                    sendJson(error("Please login first"));
                    return;
                }

                MessageRouter.sendMessageBroadcast(username, msg.content);
                break;

            case "file":
                if (!authenticated) {
                    sendJson(error("Please login first"));
                    return;
                }

                MessageRouter.sendFile(username, msg.to, msg.filename, msg.fileData);
                break;

            case "disconnect":
                cleanup();
                break;

            default:
                sendJson(error("Unknown message type"));
                break;
        }
    }

    private void handleLogin(Message msg) throws IOException {
        if (authenticated) {
            sendJson(error("Already logged in"));
            return;
        }

        if (!AuthService.authenticate(msg.username, msg.password)) {
            sendJson(simple("login_fail"));
            return;
        }

        if (!Server.registerClient(msg.username, this)) {
            sendJson(simple("login_fail"));
            return;
        }

        username = msg.username;
        authenticated = true;

        sendJson(simple("login_success"));
    }

    public void send(Message msg) {
        try {
            sendJson(gson.toJson(msg));
        } catch (IOException e) {
            System.out.println("Failed to send to " + username);
        }
    }

    private void sendJson(String json) throws IOException {
        out.writeBytes(json + "\n");
    }

    private String simple(String type) {
        Message m = new Message();
        m.type = type;
        return gson.toJson(m);
    }

    private String error(String message) {
        Message m = new Message();
        m.type = "error";
        m.content = message;
        return gson.toJson(m);
    }

    private void cleanup() {
        connected = false;

        if (authenticated) {
            Server.removeClient(username);
            authenticated = false;
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}