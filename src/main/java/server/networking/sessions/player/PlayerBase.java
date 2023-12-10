package server.networking.sessions.player;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import models.Replicate;
import server.authentication.FuseID;
import server.networking.sessions.SessionManager;

public class PlayerBase {
    @Replicate
    public String key;
    public String name;
    public String email;
    public InputStream input;
    public OutputStream output;

    public  PlayerBase(final String key, final String name, final String email, final InputStream input, final OutputStream output) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.input = input;
        this.output = output;
    }

    protected static String generatePlayerID(SessionManager manager) {
        String token = FuseID.generateToken();
        if (manager.checkIfPlayerIDisValid(token)) return generatePlayerID(manager);
        return token;
    }
}