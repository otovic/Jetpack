package server.networking.sessions.player;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import models.Replicate;
import server.networking.sessions.PlayerSessionEvents;
import server.networking.sessions.SessionManager;

public class Player<T> extends PlayerBase implements PlayerSessionEvents<SessionManager> {
    @Replicate
    public T data;
    
    public Player(final String key, final String name, final String email, final InputStream input, final OutputStream output) {
        super(key, name, email, input, output);
    }

    @Override
    public void connectPlayer(SessionManager manager) {

    }

    @Override
    public void removePlayer(SessionManager manager) {

    }

    @Override
    public void updatePlayerData(SessionManager manager) {

    }

    public static Player generateNewPlayer(SessionManager manager, final String name, final String email, final InputStream input, final OutputStream output) {
        String token = PlayerBase.generatePlayerID(manager);
        return new Player(token, name, email, input, output);
    }
}
