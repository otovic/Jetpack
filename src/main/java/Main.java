import models.RequestMethod;
import server.networking.sessions.game.GameSession;
import server.networking.sessions.player.Player;
import server.Server;
import server.authentication.FuseID;
import server.client.EventResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.ResultSet;

import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8082, false, 20, 20);
        server.setDatabase("jdbc:mysql://localhost:3306/ludofx", "root", "");

        server.corsConfig.setAllowOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.registerEvent("logout", (controller) -> {
            controller.logout();
        });

        server.registerEvent("createLobby", (controller) -> {
            System.out.println("Creating lobby");
            String lobbyID = FuseID.generateToken();
            System.out.println(lobbyID);
            controller.sessionManager.getGameSessions().put(lobbyID, new GameSession(lobbyID, controller.player, Arrays.asList(controller.player), false));
            EventResponse eventResponse = new EventResponse("createLobby", new HashMap<>() {{
                put("lobbyID", lobbyID);
            }}, new HashMap<>());
            controller.sessionManager.sendData(controller.player, eventResponse);
            System.out.println(controller.sessionManager.getGameSessions().toString());
        });

        server.registerEvent("leaveLobby", (controller) -> {
            System.out.println("Leaving lobby");
            for (Map.Entry<String, GameSession> entry : controller.sessionManager.getGameSessions().entrySet()) {
                String k = entry.getKey();
                GameSession v = entry.getValue();

                if (v.activePlayers.contains(controller.player)) {
                    controller.player.isReady = false;

                    List<Player> l = new ArrayList<>(v.activePlayers);
                    l.remove(controller.player);
                    v.activePlayers = l;

                    if (v.activePlayers.size() == 0) {
                        HashMap<String, GameSession> lobbies = controller.sessionManager.getGameSessions();
                        lobbies.remove(k);
                        controller.sessionManager.setGameSessions(lobbies);
            
                    } else if (v.owner == controller.player) {
                        v.activePlayers.forEach((p) -> {
                            p.isReady = false;
                            controller.sessionManager.sendData(p, new EventResponse("lobbyDeleted", new HashMap<>(), new HashMap<>()));
                        });
                    HashMap<String, GameSession> lobbies = controller.sessionManager.getGameSessions();
                    lobbies.remove(k);
                    controller.sessionManager.setGameSessions(lobbies);
                    } else {
                        v.activePlayers.forEach((p) -> {
                            controller.sessionManager.sendData(p, new EventResponse("playerLeftLobby", new HashMap<>() {{
                                put("playerID", controller.player.key);
                            }}, new HashMap<>()));
                        });
                    }   
                    break;
                }
            }
        });

        server.registerEvent("fetchLobbies", (controler) -> {
            System.out.println("Fetching lobbies");
            HashMap<String, String> lobbies = new HashMap<>();
            int i = 0;
            controler.sessionManager.getGameSessions().forEach((k, v) -> {
                if (v.activePlayers.size() == 4) {
                    return;
                }
                if (v.isStarted) {
                    return; 
                }
                lobbies.put("lobby" + i, v.owner.username);
            });
            EventResponse eventResponse = new EventResponse("fetchLobbies", lobbies, new HashMap<>());
            controler.sessionManager.sendData(controler.player, eventResponse);
        });

        server.registerEvent("joinLobby", (controller) -> {
            System.out.println("Joining lobby");
            String lobbyID = controller.data.eventData.get("lobbyID");
            final boolean[] found = new boolean[1];
            controller.sessionManager.getGameSessions().forEach((k, v) -> {
                if (v.owner.username.equals(lobbyID)) {
                    found[0] = true;
                    v.activePlayers.forEach((p) -> {
                        controller.sessionManager.sendData(p, new EventResponse("playerJoinedLobby", new HashMap<>() {{
                            put("playerID", controller.player.key);
                            put("username", controller.player.username);
                        }}, new HashMap<>()));
                        System.out.println("Synicng player: " + p.key + " | " + p.username + " | " + p.isReady);
                        controller.sessionManager.sendData(controller.player, new EventResponse("syncPlayer", new HashMap<>() {{
                            put("key", p.key);
                            put("username", p.username);
                            put("ready", String.valueOf(p.isReady));
                        }}, new HashMap<>()));
                    });
                    List<Player> players = new ArrayList<>(v.activePlayers);
                    players.add(controller.player);
                    v.activePlayers = players;
                    controller.sessionManager.sendData(controller.player, new EventResponse("joinLobby", new HashMap<>() {{
                        put("lobbyID", v.id);
                        put("ownerID", v.owner.key);
                    }}, new HashMap<>()));
                }
            });
            if (!found[0]) {
                controller.sessionManager.sendData(controller.player, new EventResponse("lobbyNotFound", new HashMap<>(), new HashMap<>()));
            }
        });

        server.registerEvent("sendMessage", (controller) -> {
            System.out.println("Sending message");
            System.out.println(controller.data.eventData.toString());
            String lobbyID = controller.data.eventData.get("lobbyID");
            System.out.println(lobbyID);
            String message = controller.data.eventData.get("message");
            controller.sessionManager.getGameSessions().get(lobbyID).activePlayers.forEach(player -> {
                if (player != controller.player) {
                    controller.sessionManager.sendData(player, new EventResponse("receiveMessage", new HashMap<>() {{
                        put("message", message);
                        put("username", controller.player.username);
                    }}, new HashMap<>()));
                }
            }); 
        });

        server.registerEvent("ready", (controller) -> {
            System.out.println("Player ready");
            String lobbyID = controller.data.eventData.get("lobbyID");
            controller.player.isReady = !controller.player.isReady;
            controller.sessionManager.getGameSessions().get(lobbyID).activePlayers.forEach(player -> {
                if (player != controller.player) {
                    controller.sessionManager.sendData(player, new EventResponse("playerReady", new HashMap<>() {{
                        put("playerID", controller.player.key);
                    }}, new HashMap<>()));
                }
            }); 
        });

        server.addRoute("/register", RequestMethod.POST, ((req, res) -> {
            EventResponse eventResponse = new Gson().fromJson(req.body, EventResponse.class);
            String query = "INSERT INTO players (username, email, password) VALUES ('" + eventResponse.eventParams.get("username") + "', '"
                    + eventResponse.eventParams.get("email") + "', '" + eventResponse.eventParams.get("password") + "')";
            server.database.connect();
            String result = server.database.executeQuery(query);
            
            if(result.equals("Success")) {
                server.connectClient(req, res);
            } else {
                res.rawjson(new EventResponse("registerPlayer", new HashMap<>() {{
                    put("error", result);
                }}, new HashMap<>()));
            } 
        }));

        server.addRoute("/login", RequestMethod.POST, ((req, res) -> {
            System.out.println("Login route");
            EventResponse eventResponse = new Gson().fromJson(req.body, EventResponse.class);
            String query = "SELECT * FROM players WHERE username = '" + eventResponse.eventParams.get("username") + "' AND password = '" + eventResponse.eventParams.get("password") + "'";
            server.database.connect();
            System.out.println(query);
            ResultSet result = server.database.executeQueryWithResult(query);
            if(result.next()) {
                server.connectClient(req, res);
            } else {
                res.rawjson(new EventResponse("loginPlayer", new HashMap<>() {{
                    put("error", "Invalid credentials");
                }}, new HashMap<>()));
            }
        }));


        // server.addRoute("/petar", ((req, res) -> {
        //     res.send("200 OK", "index.html");
        // }));

        server.start();
    }
}
