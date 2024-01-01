package server.networking.sessions.game;

import java.util.List;

import server.networking.sessions.player.Player;

public class GameSession {
    public String id;
    public Player owner;
    public List<Player> activePlayers;

    public GameSession(String id, Player owner, List<Player> activePlayers) {
        this.id = id;
        this.owner = owner;
        this.activePlayers = activePlayers;
    }
}
