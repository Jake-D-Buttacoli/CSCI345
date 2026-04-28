package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Connects to the server.
 * Sends & Recieves messages.
 */
class Client {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            
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

            String loginJson = "{\"type\":\"login\",\"username\":\"" + username +
                               "\",\"password\":\"" + password + "\"}";

            outToServer.writeBytes(loginJson + "\n");

            String response = inFromServer.readLine();
            if (response.contains("login_success")) {
                System.out.println("Login successful.");
            } else {
                System.out.println("Login failed: " + response);
                clientSocket.close();
                scanner.close();
                return;
            }

           
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = inFromServer.readLine()) != null) {
                        System.out.println("\nFROM SERVER: " + msg);
                        System.out.print("To (or 'quit'): ");
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected.");
                }
            }).start();

            
            while (true) {
                System.out.print("To (or 'quit'): ");
                String toUser = scanner.nextLine();

                if (toUser.equalsIgnoreCase("quit")) {
                    outToServer.writeBytes("{\"type\":\"disconnect\"}\n");
                    break;
                }

                System.out.print("Message: ");
                String content = scanner.nextLine();

                String msgJson = "{\"type\":\"message\",\"to\":\"" + toUser +
                                 "\",\"content\":\"" + content + "\"}";

                outToServer.writeBytes(msgJson + "\n");
            }

            clientSocket.close();
            scanner.close();

        } catch (IOException e) {
            System.out.println("Error connecting to server.");
            e.printStackTrace();
        }
    }
}
