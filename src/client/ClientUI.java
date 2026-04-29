package client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import shared.Message;

public class ClientUI {

    @FXML private VBox loginBox;
    @FXML private VBox messageBox;
    @FXML private VBox messageReciever;
    @FXML private VBox leftMessageContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginStatus;
    @FXML private TextField toField;
    @FXML private TextArea messageArea;
    @FXML private Label messageStatus;


    private final Client connection = new Client();

    @FXML
    private void initialize() {
        // show login UI
        loginBox.setVisible(true);
        loginBox.setManaged(true);

        messageBox.setVisible(false);
        messageBox.setManaged(false);

        messageReciever.setVisible(false);
        messageReciever.setManaged(false);
    }

    @FXML
    private void loginAttempt() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            boolean success = connection.connectAndLogin(username, password);

            if (success) {
                loginBox.setVisible(false);
                loginBox.setManaged(false);

                messageBox.setVisible(true);
                messageBox.setManaged(true);

                messageReciever.setVisible(true);
                messageReciever.setManaged(true);

                connection.startListening(msg -> {
                    Platform.runLater(() -> {
                        if (msg.type.equals("message")) {
                            addMessageBox("From: " + msg.from, msg.content);
                        } else if (msg.type.equals("broadcast")) {
                            addMessageBox("[Broadcast] " + msg.from, msg.content);
                        }
                    });
                });

            } else {
                loginStatus.setText("Login failed, try again.");
            }

        } catch (Exception e) {
            loginStatus.setText("Could not connect to server.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onSend() {
        String toUser = toField.getText();
        String message = messageArea.getText();

        boolean sent = connection.sendMessage(toUser, message);

        if (sent) {
            messageStatus.setText("Message sent.");

            // Add sent message to left message list
            addMessageBox("To: " + toUser, message);

            messageArea.clear();
        } else {
            messageStatus.setText("Message failed.");
        }
    }

    private void addMessageBox(String fromUser, String message) {
        Label fromLabel = new Label(fromUser);
        fromLabel.setStyle("-fx-font-weight: bold;");

        TextArea messageText = new TextArea(message);
        messageText.setEditable(false);
        messageText.setWrapText(true);
        messageText.setPrefRowCount(2);

        VBox singleMessageBox = new VBox(5);
        singleMessageBox.setStyle(
                "-fx-padding: 8;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: gray;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

        singleMessageBox.getChildren().addAll(fromLabel, messageText);

        leftMessageContainer.getChildren().add(singleMessageBox);
    }
}