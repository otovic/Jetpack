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
    public int color;
    public boolean isReady = false;
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

    public void setColor(final int color) {
        this.color = color;
    }

    public void setReady() {
        this.isReady = !this.isReady;
    }

    @Override
    public String toString() {
        return "Player [color=" + color + ", email=" + email + ", input=" + input + ", isReady=" + isReady + ", key="
                + key + ", output=" + output + ", username=" + username + "]";
    }
}
