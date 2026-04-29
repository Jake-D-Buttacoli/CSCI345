package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One per each client.
 * Handles incoming messages, parsing the JSON, the routing calls, & sending responses.
 */
public class ClientHandler implements Runnable {
    private static final Pattern TYPE_PATTERN = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("\"username\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TO_PATTERN = Pattern.compile("\"to\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]+)\"");

    private final Socket socket;
    private BufferedReader in;
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            String message;
            while (connected && (message = in.readLine()) != null) {
                handleMessage(message); // Commands: login, message, broadcast, disconnect
            }
        } catch (Exception e) {
            if (!connected) {
                return;
            }
            if (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
                cleanup();
                return;
            }
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String message) throws IOException {
        String type = extractValue(TYPE_PATTERN, message);

        if (type == null) {
            sendJson("{\"type\":\"error\",\"message\":\"Invalid message format\"}");
            return;
        }

        if ("login".equals(type)) {
            handleLogin(message);
            return;
        }

        if ("message".equals(type)) {
            String to = extractValue(TO_PATTERN, message);
            String content = extractValue(CONTENT_PATTERN, message);

            if (to == null || content == null) {
                sendJson("{\"type\":\"error\",\"message\":\"Invalid message format\"}");
                return;
            }

            MessageRouter.sendMessageToUser(username, to, content);
            return;
        }

        if ("broadcast".equals(type)) {
            String content = extractValue(CONTENT_PATTERN, message);
            MessageRouter.sendMessageBroadcast(username, content);
            return;
        }

        if ("disconnect".equals(type)) {
            cleanup();
            return;
        }

        if (!authenticated) {
            sendJson("{\"type\":\"error\",\"message\":\"Please login first\"}");
            return;
        }

        System.out.println("Authenticated client message received from " + username + ": " + message);
    }

    private void handleLogin(String message) throws IOException {
        if (authenticated) {
            sendJson("{\"type\":\"error\",\"message\":\"Already logged in\"}");
            return;
        }

        String requestedUsername = extractValue(USERNAME_PATTERN, message);
        String password = extractValue(PASSWORD_PATTERN, message);

        if (!AuthService.authenticate(requestedUsername, password)) {
            sendJson("{\"type\":\"login_fail\"}");
            return;
        }

        if (!Server.registerClient(requestedUsername, this)) {
            sendJson("{\"type\":\"login_fail\"}");
            return;
        }

        username = requestedUsername;
        authenticated = true;
        sendJson("{\"type\":\"login_success\"}");
    }

    private void sendJson(String json) throws IOException {
        out.writeBytes(json + "\n");
    }

    public void send(String json) { // Publicly used by the message router
        try {
            sendJson(json);
        } catch (IOException e) {
            System.out.println(" -!- Failed to send message to " + username);
        }
    }

    private String extractValue(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String safeUsername() {
        return username == null ? "unknown" : escapeJson(username);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
