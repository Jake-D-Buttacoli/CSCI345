package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import com.google.gson.Gson;
import shared.Message;

/**
 * Connects to the server.
 * Sends & Recieves messages.
 */
class Client {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            Gson gson = new Gson();


            Socket clientSocket = new Socket("localhost", 6789);

            BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
            );

            DataOutputStream outToServer = new DataOutputStream(
                    clientSocket.getOutputStream()
            );

            System.out.println("Connected to server.");


            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            Message loginMsg = new Message();
            loginMsg.type = "login";
            loginMsg.username = username;
            loginMsg.password = password;

            outToServer.writeBytes(gson.toJson(loginMsg) + "\n");

            new Thread(() -> {      // Separate thread for listening to server. Immediately prints.
                try {
                    String msg;
                    while ((msg = inFromServer.readLine()) != null) {
                        System.out.println("\nFROM SERVER: " + msg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected.");
                }
            }).start();


            while (true) {
                System.out.print("To (or 'all' or 'quit'): ");
                String toUser = scanner.nextLine();

                if (toUser.equalsIgnoreCase("quit")) {
                    outToServer.writeBytes("{\"type\":\"disconnect\"}\n");
                    break;
                }

                System.out.print("Message: ");
                String content = scanner.nextLine();

                Message msg = new Message();

                if (toUser.equalsIgnoreCase("all")) {
                    msg.type = "broadcast";
                    msg.content = content;
                } else {
                    msg.type = "message";
                    msg.to = toUser;
                    msg.content = content;
                }

                outToServer.writeBytes(gson.toJson(msg) + "\n");
            }

            clientSocket.close();
            scanner.close();

        } catch (IOException e) {
            System.out.println("Error connecting to server.");
            e.printStackTrace();
        }
    }
}
