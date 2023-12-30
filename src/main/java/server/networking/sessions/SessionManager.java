package server.networking.sessions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.EventTask;
import server.multithreading.Hook;
import server.networking.sessions.event.EventHandler;
import server.networking.sessions.game.GameSession;
import server.networking.sessions.player.Player;

public class SessionManager {
    // public List<String> takenGSIDs = new ArrayList<>();
    // public HashMap<String, GameSession<GS>> gameSessions = new HashMap<>();
    // public HashMap<String, Player<PS>> activePlayers = new HashMap<>();
    // private EventHandler<PS, GS> eventHandler = new EventHandler<PS, GS>(this);
    // private Class<PS> playerStateType;
    // private Hook hook;

    // public <T> SessionManager(Hook hook, Class<T> type) {
    //     this.hook = hook;
    //     this.playerStateType = (Class<PS>) type;
    //     this.eventHandler.addDefaultEvents();
    // }

    // @Override
    // public void connectPlayer(Player player) {
    //     System.out.println("ADDING NEW PLAYER");
    //     activePlayers.put(player.key, player);
    //     try {
    //         this.hook.addListener(player, this);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     System.out.println(activePlayers.toString());
    // }

    // @Override
    // public void removePlayer(Player player) {
    //     activePlayers.remove(player);
    // }

    // public <T> void updatePlayerData(final String token, final T data) {
    //     Player<PS> player = activePlayers.get(token);
    //     player.updatePlayerData((PS) data);
    //     activePlayers.replace(token, player);
    //     System.out.println(player.state);
    // }

    // public boolean addGameSessionID(String id) {
    //     this.takenGSIDs.add(id);
    //     return true;
    // }

    // public void addGameSession(String id, GameSession gs) {
    //     this.gameSessions.put(id, gs);
    // }

    // public boolean checkIfGSIDExists(String id) {
    //     final boolean[] exists = {false};
    //     this.takenGSIDs.forEach((k) -> {
    //         if(k == id) {
    //             exists[0] = true;
    //         }
    //     });
    //     return exists[0];
    // }

    // public void registerEvent(String eventName, EventTask event) {
    //     eventHandler.registerEvent(eventName, event);
    // }

    // public void fireEvent(String eventName, String data) {
    //     eventHandler.fireEvent(eventName, data);
    // }

    // public boolean fireNativeEvent(String eventName, String data, InputStream input, OutputStream output) {
    //     return eventHandler.fireNativeEvent(eventName, data, input, output);
    // }

    // public boolean checkIfPlayerIDisValid(String id) {
    //     return activePlayers.containsKey(id) ? true : false;
    // }

    // @Override
    // public void updatePlayerData(Player<?> param) {
    //     throw new UnsupportedOperationException("Unimplemented method 'updatePlayerData'");
    // }
}
