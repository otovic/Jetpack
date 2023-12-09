package server.networking.sessions.player;

import java.io.OutputStream;
import java.net.Socket;

import server.authentication.FuseID;
import server.networking.sessions.SessionManager;

abstract class PlayerBase {
    public String key;
    public String name;
    public String email;
    public Socket socket;
    public OutputStream stream;

    public  PlayerBase(final String key, final String name, final String email, final Socket socket, final OutputStream stream) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.socket = socket;
        this.stream = stream;
    }

    protected static String generatePlayerID(SessionManager manager) {
        String token = FuseID.generateToken();
        if (manager.checkIfPlayerIDisValid(token)) return generatePlayerID(manager);
        return token;
    }
}