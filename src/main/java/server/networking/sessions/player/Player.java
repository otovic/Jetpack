package server.networking.sessions.player;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import models.Replicate;
import server.networking.sessions.SessionManager;
import test_classes.PlayerR;

public class Player {
    public String key;
    public String username;
    public String email;
    public InputStream input;
    public OutputStream output;
    
    public Player(final String key, final String username, final String email, final InputStream input, final OutputStream output) {
        this.key = key;
        this.username = username;
        this.email = email;
        this.input = input;
        this.output = output;
    }
}
