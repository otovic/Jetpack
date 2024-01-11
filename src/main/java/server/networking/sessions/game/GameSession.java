package server.networking.sessions.game;

import java.util.HashMap;
import java.util.List;

import server.networking.sessions.player.Player;

/**
 * GameSession predstavlja jednu lobi sesiju igraca.
 */
public class GameSession {
    public String id;
    public Player owner;
    public boolean isStarted;
    public HashMap<String, Move> move = new HashMap<>();
    public List<Player> activePlayers;

    /**
     * Konstruktor za GameSession klasu.
     * 
     * @param id            ID lobija
     * @param owner         igrac koji je vlasnik lobija
     * @param activePlayers lista igraca koji su u lobiju
     * @param isStarted     da li je igra pocela
     */
    public GameSession(String id, Player owner, List<Player> activePlayers, boolean isStarted) {
        this.id = id;
        this.owner = owner;
        this.activePlayers = activePlayers;
        this.isStarted = isStarted;
    }

    @Override
    public String toString() {
        return "GameSession [activePlayers=" + activePlayers + ", id=" + id + ", isStarted=" + isStarted + ", move="
                + move + ", owner=" + owner + "]";
    }
}
