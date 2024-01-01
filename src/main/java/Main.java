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
            controller.sessionManager.getGameSessions().put(lobbyID, new GameSession(lobbyID, controller.player, Arrays.asList(controller.player)));
            EventResponse eventResponse = new EventResponse("createLobby", new HashMap<>() {{
                put("lobbyID", lobbyID);
            }}, new HashMap<>());
            controller.sessionManager.sendData(controller.player, eventResponse);
            System.out.println(controller.sessionManager.getGameSessions().toString());
        });

        server.registerEvent("leaveLobby", (controller) -> {
            System.out.println("Leaving lobby");
            controller.sessionManager.getGameSessions().forEach((k, v) -> {
                if (v.activePlayers.contains(controller.player)) {
                    //why ???
                    List<Player> l = new ArrayList<>(v.activePlayers);
                    l.remove(controller.player);
                    v.activePlayers = l;
                    if (v.activePlayers.size() == 0) {
                        controller.sessionManager.getGameSessions().remove(k);
                        System.out.println("Lobby deleted " + controller.sessionManager.getGameSessions().toString());
                    } else if (v.owner == controller.player) {
                        v.activePlayers.forEach((p) -> {
                            controller.sessionManager.sendData(p, new EventResponse("lobbyDeleted", new HashMap<>(), new HashMap<>()));
                        });
                        controller.sessionManager.getGameSessions().remove(k);

                    } else {
                        v.activePlayers.forEach((p) -> {
                            controller.sessionManager.sendData(p, new EventResponse("playerLeftLobby", new HashMap<>() {{
                                put("playerID", controller.player.key);
                            }}, new HashMap<>()));
                        });
                    }
                }
            });
        });

        server.registerEvent("fetchLobbies", (controler) -> {
            System.out.println("Fetching lobbies");
            HashMap<String, String> lobbies = new HashMap<>();
            int i = 0;
            controler.sessionManager.getGameSessions().forEach((k, v) -> {
                lobbies.put("lobby" + i, k);
            });
            EventResponse eventResponse = new EventResponse("fetchLobbies", lobbies, new HashMap<>());
            controler.sessionManager.sendData(controler.player, eventResponse);
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
