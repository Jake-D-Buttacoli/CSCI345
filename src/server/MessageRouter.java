package server;

/**
 * Controls the message traffic.
 * Sends a message to a specific user or to all.
 */
import shared.Message;

public class MessageRouter {

    public static void sendMessageToUser(String from, String to, String content) {
        ClientHandler recipient = Server.clients.get(to);

        if (recipient == null) {
            System.out.println("User not online: " + to);
            return;
        }

        Message msg = new Message();
        msg.type = "message";
        msg.from = from;
        msg.to = to;
        msg.content = content;

        recipient.send(msg);
    }

    public static void sendMessageBroadcast(String from, String content) {
        for (String username : Server.clients.keySet()) {
            ClientHandler recipient = Server.clients.get(username);

            if (recipient != null && !username.equals(from)) {
                Message msg = new Message();
                msg.type = "broadcast";
                msg.from = from;
                msg.content = content;

                recipient.send(msg);
            }
        }
    }

    public static void sendFile(String from, String to, String filename, String fileData) {
        ClientHandler recipient = Server.clients.get(to);

        if (recipient == null) {
            System.out.println("User not online: " + to);
            return;
        }

        Message msg = new Message();
        msg.type = "file";
        msg.from = from;
        msg.to = to;
        msg.filename = filename;
        msg.fileData = fileData;

        recipient.send(msg);
    }
}