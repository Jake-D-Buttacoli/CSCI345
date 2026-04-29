package client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;
import com.google.gson.Gson;
import shared.Message;

/**
 * Connects to the server.
 * Sends & Recieves messages.
 */
class Client {
    private static final int DEFAULT_PORT = 6789;

    /**
     * Starts our client
     */
    public static void run(int port) {
        try {
            Scanner scanner = new Scanner(System.in);
            Gson gson = new Gson();
            Socket clientSocket = new Socket("localhost", port);
            BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
            );
            DataOutputStream outToServer = new DataOutputStream(
                    clientSocket.getOutputStream()
            );
            System.out.println("Connected to server on port " + port); // Connection Established

            // Attempt Login
            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            Message loginMsg = new Message();
            loginMsg.type = "login";
            loginMsg.username = username;
            loginMsg.password = password;

            outToServer.writeBytes(gson.toJson(loginMsg) + "\n");

            // WAIT for login response
            String responseLine = inFromServer.readLine();
            Message response = gson.fromJson(responseLine, Message.class);

            if (response.type.equals("login_success")) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Login failed. Disconnected.");
                clientSocket.close();
                scanner.close();
                return;
            }

            new Thread(() -> {      // Separate thread for listening to server. Immediately prints.
                try {
                    String line;
                    while ((line = inFromServer.readLine()) != null) {
                        Message msg = gson.fromJson(line, Message.class);

                        switch (msg.type) {
                            case "message":
                                System.out.println("\n" + msg.from + ": " + msg.content);
                                break;
                            case "broadcast":
                                System.out.println("\n[BROADCAST] " + msg.from + ": " + msg.content);
                                break;
                            case "file":
                                System.out.println("\nReceiving file from " + msg.from + ": " + msg.filename);
                                saveFile(msg.filename, msg.fileData);
                                System.out.println("File saved.");
                                break;
                            default:
                                System.out.println("\nFROM SERVER: " + line);
                        }
                        System.out.print("To (or 'all' or 'file' or 'quit'): ");
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected.");
                }
            }).start();


            while (true) {
                System.out.print("To (or 'all' or 'file' or 'quit'): ");
                String toUser = scanner.nextLine();

                if (toUser.equalsIgnoreCase("quit")) {
                    outToServer.writeBytes("{\"type\":\"disconnect\"}\n");
                    break;
                }

                if (toUser.equalsIgnoreCase("file")) {
                    System.out.print("Send to: ");
                    String recipient = scanner.nextLine();

                    System.out.print("File path: ");
                    String path = scanner.nextLine();

                    File file = new File(path);

                    Message msg = new Message();
                    msg.type = "file";
                    msg.to = recipient;
                    msg.filename = file.getName();
                    msg.fileData = encodeFileToBase64(path);

                    outToServer.writeBytes(gson.toJson(msg) + "\n");
                    continue;
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

    /**
     * Encodes a file to base64 before transmission
     * @param path Filepath
     * @return The file encoded into Base64 (text-safe)
     * @throws IOException Path may not exist
     */
    private static String encodeFileToBase64(String path) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * Decodes a file after receiving & saves to directory
     * @param filename Name of file, will use when writing
     * @param base64Data received file data in base64
     * @throws IOException May be unable to write file.
     */
    private static void saveFile(String filename, String base64Data) throws IOException {
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

        File dir = new File("downloads");
        if (!dir.exists()) {
            dir.mkdirs(); // create folder if doesnt exist
        }

        Files.write(Paths.get("downloads/received_" + filename), fileBytes);
    }

    public static void main(String[] args) {
        run(DEFAULT_PORT);
    }
}
