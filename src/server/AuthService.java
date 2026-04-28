package server;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the login system.
 * Verifies the username/password
 */
public class AuthService {
    private static final Map<String, String> CREDENTIALS = new HashMap<>();

    static {
        CREDENTIALS.put("adham", "pass123");
        CREDENTIALS.put("jake", "pass123");
        CREDENTIALS.put("josh", "pass123");
        CREDENTIALS.put("sean", "pass123");
    }

    private AuthService() {
    }

    public static boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        String storedPassword = CREDENTIALS.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }
}
