import models.RequestMethod;
import server.networking.sessions.game.GameSession;
import server.networking.sessions.game.Move;
import server.networking.sessions.player.Player;
import server.Server;
import server.authentication.FuseID;
import server.client.EventResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            try {
                System.out.println("Creating lobby");
                String lobbyID = FuseID.generateToken();
                System.out.println(lobbyID);
                controller.sessionManager.getGameSessions().put(lobbyID, new GameSession(lobbyID, controller.player, Arrays.asList(controller.player), false));
                EventResponse eventResponse = new EventResponse("createLobby", new HashMap<>() {{
                    put("lobbyID", lobbyID);
                }}, new HashMap<>());
                controller.sessionManager.sendData(controller.player, eventResponse);
                System.out.println(controller.sessionManager.getGameSessions().toString());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("leaveLobby", (controller) -> {
            try {
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
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("fetchLobbies", (controler) -> {
            try {
                System.out.println("Fetching lobbies");
                HashMap<String, String> lobbies = new HashMap<>();
                final int[] i = {0};
                controler.sessionManager.getGameSessions().forEach((k, v) -> {
                    if (v.activePlayers.size() == 4) {
                        return;
                    }
                    if (v.isStarted) {
                        return; 
                    }
                    lobbies.put("lobby" + i[0], v.owner.username);
                    i[0]++;
                });
                System.out.println(lobbies.toString());
                EventResponse eventResponse = new EventResponse("fetchLobbies", lobbies, new HashMap<>());
                controler.sessionManager.sendData(controler.player, eventResponse);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("joinLobby", (controller) -> {
            try {
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
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("sendMessage", (controller) -> {
            try {
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
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("ready", (controller) -> {
            try {
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
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("startGame", (controller) -> {
            try {
                System.out.println("Starting game");
                List<Integer> colors = new ArrayList<>();
                colors.add(1);
                colors.add(2);
                colors.add(3);
                colors.add(4);
                List<Integer> generated = new ArrayList<>();

                String lobbyID = controller.data.eventData.get("lobbyID");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.put("playersStarted", new Move(0, controller.player.key, ""));
                gameSession.move.put("rolledDice", new Move(0, controller.player.key, ""));
                gameSession.move.put("newPlayerTurn", new Move(0, controller.player.key, "0"));
                gameSession.move.put("syncShowMoves", new Move(0, controller.player.key, ""));
                gameSession.move.put("syncMoveFigure", new Move(0, controller.player.key, ""));

                gameSession.isStarted = true;

                gameSession.activePlayers.forEach(player -> {
                    int num = new Random().nextInt(gameSession.activePlayers.size()) + 1;

                    while (generated.contains(num)) {
                        num = new Random().nextInt(gameSession.activePlayers.size()) + 1;
                    }

                    generated.add(num);

                    player.setColor(num);

                    gameSession.activePlayers.forEach(p -> {
                        controller.sessionManager.sendData(p, new EventResponse("assignColor", new HashMap<>() {{
                            put("playerID", player.key);
                            put("color", String.valueOf(player.color));
                        }}, new HashMap<>()));
                    });
                });

                gameSession.activePlayers.forEach(player -> {
                    controller.sessionManager.sendData(player, new EventResponse("startGame", new HashMap<>(), new HashMap<>()));
                });

            } catch (Exception e) {
                String lobbyID = controller.data.eventData.get("lobbyID");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);
                gameSession.isStarted = true;
                gameSession.activePlayers.forEach(player -> {
                    controller.sessionManager.sendData(player, new EventResponse("error", new HashMap<>(), new HashMap<>()));
                });
            }
        });

        server.registerEvent("syncStartGame", (controller) -> {
            try {
                System.out.println("syncStartGame");
                String lobbyID = controller.data.eventData.get("lobbyID");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.get("playersStarted").playerFinished++;

                if (gameSession.move.get("playersStarted").playerFinished == gameSession.activePlayers.size()) {
                    gameSession.move.remove("playersStarted");

                    gameSession.activePlayers.forEach(player -> {
                        controller.sessionManager.sendData(player, new EventResponse("initGame", new HashMap<>(), new HashMap<>()));
                    });
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("rolledDice", (controller) -> {
            try {
                System.out.println("Rolled dice");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.get("rolledDice").data = controller.data.eventData.get("rolled");
                gameSession.move.get("rolledDice").owningPlayer = controller.player.key;

                gameSession.activePlayers.forEach(player -> {
                    controller.sessionManager.sendData(player, new EventResponse("rolledDice", new HashMap<>() {{
                        put("playerID", controller.player.key);
                        put("diceValue", controller.data.eventData.get("rolled"));
                    }}, new HashMap<>()));
                });
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("syncRolledDice", (controller) -> {
            try {
                System.out.println("Finished rolling");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.get("rolledDice").playerFinished++;

                if (gameSession.move.get("rolledDice").playerFinished == gameSession.activePlayers.size()) {
                    gameSession.move.get("rolledDice").playerFinished = 0;

                    gameSession.activePlayers.forEach(player -> {
                        controller.sessionManager.sendData(player, new EventResponse("initShowMoves", new HashMap<>() {{
                            put("playerID", gameSession.move.get("rolledDice").owningPlayer);
                            put("diceValue", gameSession.move.get("rolledDice").data);
                        }}, new HashMap<>()));
                });
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("syncShowMoves", (controller) -> {
            try {
                System.out.println("Syncing show moves");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.get("syncShowMoves").playerFinished++;

                if (gameSession.move.get("syncShowMoves").playerFinished == gameSession.activePlayers.size()) {
                    gameSession.move.get("syncShowMoves").playerFinished = 0;

                    gameSession.activePlayers.forEach(player -> {
                        controller.sessionManager.sendData(player, new EventResponse("initNewPlayerTurn", new HashMap<>() {{
                            put("playerID", gameSession.move.get("rolledDice").owningPlayer);
                            put("diceValue", gameSession.move.get("rolledDice").data);
                        }}, new HashMap<>()));
                    });
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("syncNewPlayerMove", (controller) -> {
            try {
                System.out.println("Syncing new player move");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.get("newPlayerTurn").playerFinished++;

                if (gameSession.move.get("newPlayerTurn").playerFinished == gameSession.activePlayers.size()) {
                    gameSession.move.get("newPlayerTurn").playerFinished = 0;

                    gameSession.activePlayers.forEach(player -> {
                        controller.sessionManager.sendData(player, new EventResponse("initNewPlayerTurn", new HashMap<>() {{
                            put("playerID", gameSession.move.get("rolledDice").owningPlayer);
                            put("diceValue", gameSession.move.get("rolledDice").data);
                        }}, new HashMap<>()));
                    });
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("moveFigure", (controller) -> {
            try {
                System.out.println("Moving figure");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                final String rolled = controller.data.eventData.get("rolled");
                final String figure = controller.data.eventData.get("figure");

                gameSession.move.get("syncMoveFigure").data = controller.data.eventData.get("figure");
                gameSession.move.get("syncMoveFigure").owningPlayer = controller.player.key;

                gameSession.activePlayers.forEach(player -> {
                    controller.sessionManager.sendData(player, new EventResponse("moveFigure", new HashMap<>() {{
                        put("playerID", controller.player.key);
                        put("figure", figure);
                        put("rolled", rolled);
                    }}, new HashMap<>()));
                });
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("syncMoveFigure", (controller) -> {
            try {
                System.out.println("Finished moving figure");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);

                gameSession.move.get("syncMoveFigure").playerFinished++;

                if (gameSession.move.get("syncMoveFigure").playerFinished == gameSession.activePlayers.size()) {
                    gameSession.move.get("syncMoveFigure").playerFinished = 0;
                    gameSession.move.get("syncShowMoves").playerFinished = 0;

                    gameSession.activePlayers.forEach(player -> {
                        controller.sessionManager.sendData(player, new EventResponse("initNewPlayerTurn", new HashMap<>() {{
                            put("playerID", gameSession.move.get("syncMoveFigure").owningPlayer);
                            put("diceValue", gameSession.move.get("rolledDice").data);
                        }}, new HashMap<>()));
                    });
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.registerEvent("gameOver", (controller) -> {
            try {
                System.out.println("Game over");
                String lobbyID = controller.data.eventData.get("game");
                GameSession gameSession = controller.sessionManager.getGameSessions().get(lobbyID);
                gameSession.isStarted = false;
                gameSession.activePlayers.forEach(player -> {
                    player.isReady = false;
                });
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        server.addRoute("/register", RequestMethod.POST, ((req, res) -> {
            try {
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
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));

        server.addRoute("/login", RequestMethod.POST, ((req, res) -> {
            try {
                System.out.println("Login route");
                EventResponse eventResponse = new Gson().fromJson(req.body, EventResponse.class);
                String query = "SELECT * FROM players WHERE username = '" + eventResponse.eventParams.get("username") + "' AND password = '" + eventResponse.eventParams.get("password") + "'";
                server.database.connect();
                ResultSet result = server.database.executeQueryWithResult(query);
                if(result.next()) {
                    server.connectClient(req, res);
                } else {
                    res.rawjson(new EventResponse("loginPlayer", new HashMap<>() {{
                        put("error", "Invalid credentials");
                    }}, new HashMap<>()));
                }
            } catch(SQLException sql) {
                System.out.println("Error: " + sql.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }));


        // server.addRoute("/petar", ((req, res) -> {
        //     res.send("200 OK", "index.html");
        // }));

        server.start();
    }
}
