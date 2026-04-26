package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Starts the server.
 * Accepts Connections/Creates the ClientHandler threads.
 */
public class Server {

    /**
     * A map to store a list of clients with their usernames
     */
    public static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    /**
     * Starts our server
     * @param port The port # is arbitrary, but the clients & server must all agree on what we choose
     * @throws Exception
     */
    public static void start(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);

        while(true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }
}
