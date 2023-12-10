package server.networking.sessions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import server.networking.sessions.game.GameSession;
import server.networking.sessions.player.Player;

public class SessionManager<PS> implements PlayerSessionEvents<Player<?>> {
    public List<String> takenGSIDs = new ArrayList<>();
    public HashMap<String, GameSession<PS>> gameSessions = new HashMap<>();
    public HashMap<String, Player<PS>> activePlayers = new HashMap<>();

    @Override
    public void connectPlayer(Player player) {
        System.out.println("ADDING NEW PLAYER");
        activePlayers.put(player.key, player);
        System.out.println(activePlayers.toString());
    }

    @Override
    public void removePlayer(Player player) {
        activePlayers.remove(player);
    }

    @Override
    public void updatePlayerData(Player player) {
        activePlayers.replace(null, player);
    }

    public boolean addGameSessionID(String id) {
        this.takenGSIDs.add(id);
        return true;
    }

    public void addGameSession(String id, GameSession gs) {
        this.gameSessions.put(id, gs);
    }

    public boolean checkIfGSIDExists(String id) {
        final boolean[] exists = {false};
        this.takenGSIDs.forEach((k) -> {
            if(k == id) {
                exists[0] = true;
            }
        });
        return exists[0];
    }

    public boolean checkIfPlayerIDisValid(String id) {
        return activePlayers.containsKey(id) ? true : false;
    }
}
