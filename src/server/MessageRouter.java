package server;

import shared.Message;

/**
 * Controls the message traffic.
 * Sends a message to a specific user or to all.
 */
public class MessageRouter {
    /**
     *  Finds the reciever, builds json to send, & sends through their socket.
     * @param from sender
     * @param to reciever
     * @param content message
     */
    public static void sendMessageToUser(String from, String to, String content) {
        ClientHandler target = Server.clients.get(to);

        if (target == null) {
            System.out.println("User " + to + " is not online.");
            return;
        }

        Message msg = new Message();
        msg.type = "message";
        msg.from = from;
        msg.content = content;

        target.send(msg);
    }

    /**
     * Similar to sending direct message, but sends for each user currently connected to the server.
     * @param from sender
     * @param content message
     */
    public static void sendMessageBroadcast(String from, String content) {
        Message msg = new Message();
        msg.type = "broadcast";
        msg.from = from;
        msg.content = content;

        for (ClientHandler client : Server.clients.values()) {
            client.send(msg);
        }
    }
}
