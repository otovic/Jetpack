package server.networking.sessions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import models.Event;
import server.authentication.FuseID;
import server.client.EventResponse;
import server.client.Request;
import server.client.Response;
import server.multithreading.Hook;
import server.networking.sessions.event.EventHandler;
import server.networking.sessions.player.Player;

public class SessionManager {
    private EventHandler eventHandler = new EventHandler();
    private Hook hook;
    private HashMap<String, Player> playerSessions = new HashMap<>();

    public SessionManager(Hook hook) {
        this.hook = hook;
    }

    public void registerEvent(String eventName, Event event) {
        this.eventHandler.registerEvent(eventName, event);
    }

    public void connectPlayer(final Request req, final Response res) throws IOException {
        System.out.println("ADDING NEW PLAYER");
        EventResponse data = new Gson().fromJson(req.body, EventResponse.class);
        String key = generatePlayerID();
        Player player = new Player(key, data.eventParams.get("username"), data.eventParams.get("email"), res.getSocket().getInputStream(), res.getSocket().getOutputStream());
        this.playerSessions.put(player.key, player);
        this.sendConnection(res.getSocket().getOutputStream());
        this.hook.addListener(player, this);
        System.out.println(this.playerSessions.toString());
    }

    public void disconnectPlayer(final Player player) {
        this.playerSessions.remove(player.key);
        System.out.println(this.playerSessions.toString());
    }

    public void executeEvent(final String eventName, final Player p, final EventResponse eventResponse) {
        this.eventHandler.executeEvent(eventName, p, eventResponse, this);
    }

    public String generatePlayerID() {
        String key = FuseID.generateToken();
        while (playerSessions.containsKey(key)) {
            key = FuseID.generateToken();
        }
        return key;
    }

    private void sendConnection(final OutputStream out) {
        try {
            System.out.println("SENDING CONNECTION");
            PrintWriter writer = new PrintWriter(out);
            writer.println("PETAR");
            writer.flush();
        } catch (Exception e) {
            System.out.println("Failed to send connection");
        }
    }

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
