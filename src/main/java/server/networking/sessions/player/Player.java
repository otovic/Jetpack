package server.networking.sessions.player;

import java.io.OutputStream;
import java.net.Socket;

import server.networking.sessions.PlayerSessionEvents;
import server.networking.sessions.SessionManager;

public class Player<T> extends PlayerBase implements PlayerSessionEvents<SessionManager> {
    public T data;
    
    public Player(final String key, final String name, final String email, final Socket socket, final OutputStream stream) {
        super(key, name, email, socket, stream);
    }

    @Override
    public void addPlayer(SessionManager manager) {

    }

    @Override
    public void removePlayer(SessionManager manager) {

    }

    @Override
    public void updatePlayerData(SessionManager manager) {

    }

    public static Player generateNewPlayer(SessionManager manager, final String name, final String email, final Socket socket, final OutputStream stream) {
        String token = PlayerBase.generatePlayerID(manager);
        return new Player(token, name, email, socket, stream);
    }
}
