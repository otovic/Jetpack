package server.authentication;

import java.util.Random;

public class FuseID {
    public String token;

    public FuseID(String token) {
        this.token = token;
    }

    public static String generateToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            token.append(characters.charAt(r.nextInt(61) + 1));
        }
        return token.toString();
    }
}
