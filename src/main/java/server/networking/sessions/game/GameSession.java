package server.networking.sessions.game;

import java.util.List;

import server.networking.sessions.player.Player;

public class GameSession<T> {
    public Player<T> owner;
    public List<Player<T>> activePlayers;

    public void onRep() {

    }
}
