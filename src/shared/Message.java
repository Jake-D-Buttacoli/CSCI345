package shared;

public class Message {
    public String type;

    // login
    public String username;
    public String password;

    // chat messages
    public String from;
    public String to;
    public String content;

    // file messages
    public String filename;
    public String fileData;
}
