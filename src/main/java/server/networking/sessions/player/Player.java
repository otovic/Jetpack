package server.networking.sessions.player;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import models.Replicate;
import server.networking.sessions.PlayerSessionEvents;
import server.networking.sessions.SessionManager;
import test_classes.PlayerR;

public class Player<T> {
    @Replicate
    public String key;
    public String email;
    public InputStream input;
    public OutputStream output;
    public T state;
    
    public Player(final String key, final String email, final T state, final InputStream input, final OutputStream output) {
        this.key = key;
        this.email = email;
        this.state = state;
        this.input = input;
        this.output = output;
    }

    public static <T> Player generateNewPlayer(SessionManager manager, final String email, final T data, final InputStream input, final OutputStream output) {
        String token = PlayerBase.generatePlayerID(manager);
        return new Player(token, email, data, input, output);
    }

    public void updatePlayerData(T param) {
        this.state = param;
    }
}
