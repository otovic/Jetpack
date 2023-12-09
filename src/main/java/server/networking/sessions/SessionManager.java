package server.networking.sessions;

import java.util.HashMap;

import server.networking.sessions.player.Player;

public class SessionManager<GS, PS> implements PlayerSessionEvents<Player> {
    public HashMap<String, GS> gameSessions;
    public HashMap<String, Player<PS>> activePlayers;

    @Override
    public void addPlayer(Player player) {
        System.out.println("ADDING NEW PLAYER");
        activePlayers.put(null, player);
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

    public boolean checkIfPlayerIDisValid(String id) {
        return activePlayers.containsKey(id) ? true : false;
    }
}
