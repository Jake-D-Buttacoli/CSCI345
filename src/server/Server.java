package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Starts the server.
 * Accepts Connections/Creates the ClientHandler threads.
 */
public class Server {
    private static final int DEFAULT_PORT = 6789;
    /**
     * A map to store a list of all currently logged in clients with their usernames
     */
    public static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    /**
     * Starts our server
     * @param port The port # is arbitrary, but the clients & server must all agree on what we choose
     * @throws Exception
     */
    public static void start(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while(true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    public static boolean registerClient(String username, ClientHandler handler) {
        if (username == null || handler == null || clients.containsKey(username)) {
            return false;
        }

        clients.put(username, handler);
        System.out.println("STATUS: " + username + " is online.");
        return true;
    }

    public static void removeClient(String username) {
        if (username == null) {
            return;
        }

        ClientHandler removed = clients.remove(username);
        if (removed != null) {
            System.out.println("STATUS: " + username + " is offline.");
        }
    }

    public static boolean isUserOnline(String username) {
        return clients.containsKey(username);
    }

    public static void main(String[] args) throws Exception {
        start(DEFAULT_PORT);
    }
}
