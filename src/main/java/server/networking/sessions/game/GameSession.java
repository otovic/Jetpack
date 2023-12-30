package server.networking.sessions.game;

import java.util.List;

import server.networking.sessions.player.Player;

public class GameSession<T> {
    public Player owner;
    public List<Player> activePlayers;

    public void onRep() {

    }
}
