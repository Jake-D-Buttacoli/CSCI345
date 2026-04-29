package shared;

/**
 * Simple helper class.
 * Representing the message info.
 */
public class Message {
    public String type;

    // login
    public String username;
    public String password;

    // messaging
    public String to;
    public String from;
    public String content;

    // file transfer
    public String filename;
    public String fileData; // base64 encode
}
