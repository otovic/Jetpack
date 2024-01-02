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
import server.networking.sessions.game.GameSession;
import server.networking.sessions.player.Player;

public class SessionManager {
    private EventHandler eventHandler = new EventHandler();
    private Hook hook;
    private HashMap<String, Player> playerSessions = new HashMap<>();
    private HashMap<String, GameSession> gameSessions = new HashMap<>();

    public SessionManager(Hook hook) {
        this.hook = hook;
    }

    public synchronized HashMap<String, GameSession> getGameSessions() {
        return this.gameSessions;
    }

    public synchronized void setGameSessions(final HashMap<String, GameSession> gameSessions) {
        this.gameSessions = gameSessions;
    }

    public void registerEvent(String eventName, Event event) {
        this.eventHandler.registerEvent(eventName, event);
    }

    public void connectPlayer(final Request req, final Response res) throws IOException {
        EventResponse data = new Gson().fromJson(req.body, EventResponse.class);
        String key = generatePlayerID();
        Player player = new Player(key, data.eventParams.get("username"), data.eventParams.get("email"), res.getSocket().getInputStream(), res.getSocket().getOutputStream());
        this.playerSessions.put(player.key, player);
        this.sendConnection(res.getSocket().getOutputStream(), player.key);
        this.hook.addListener(player, this);
    }

    public void disconnectPlayer(final Player player) {
        this.playerSessions.remove(player.key);
        this.gameSessions.forEach((k, v) -> {
            if (v.owner == player) {
                v.activePlayers.forEach((p) -> {
                    this.sendData(p, new EventResponse("lobbyDeleted", new HashMap<>(), new HashMap<>()));
                });
                this.gameSessions.remove(k);
            }
            if (v.activePlayers.contains(player)) {
                v.activePlayers.remove(player);
                if (v.activePlayers.size() == 0) {
                    this.gameSessions.remove(k);
                } else {
                    v.activePlayers.forEach((p) -> {
                        this.sendData(p, new EventResponse("playerLeftLobby", new HashMap<>() {{
                            put("playerID", player.key);
                            put("username", player.username);
                        }}, new HashMap<>()));
                    });
                }
            }
        });
        System.out.println("DELETING: " + player.key + " | " + this.playerSessions.toString());
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

    private void sendConnection(final OutputStream out, final String key) {
        try {
            EventResponse response = new EventResponse("connectPlayer", new HashMap<String, String>() {{
                put("key", key);
            }}, new HashMap<>());
            PrintWriter writer = new PrintWriter(out);
            writer.println(new Gson().toJson(response));
            writer.flush();
        } catch (Exception e) {
            System.out.println("Failed to send connection");
        }
    }

    public void sendData(final Player player, final EventResponse data) {
        try {
            PrintWriter writer = new PrintWriter(player.output);
            writer.println(new Gson().toJson(data));
            writer.flush();
        } catch (Exception e) {
            System.out.println("Failed to send data");
        }
    }

    public void sendData(final String lobbyID, final EventResponse data) {
        try {
            GameSession gameSession = this.gameSessions.get(lobbyID);
            gameSession.activePlayers.forEach((player) -> {
                try {
                    PrintWriter writer = new PrintWriter(player.output);
                    writer.println(new Gson().toJson(data));
                    writer.flush();
                } catch (Exception e) {
                    System.out.println("Failed to send data");
                }
            });
        } catch (Exception e) {
            System.out.println("Failed to send data");
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
