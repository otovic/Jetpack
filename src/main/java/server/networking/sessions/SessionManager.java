package server.networking.sessions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

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

/**
 * SessionManager klasa se koristi za upravljanje sesijama.
 */
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

    /**
     * Registruje event u event handler.
     *
     * @param eventName ime eventa
     * @param event kod koji ce se izvrsiti kada se event pozove
     */
    public void registerEvent(String eventName, Event event) {
        this.eventHandler.registerEvent(eventName, event);
    }

    /**
     * Dodaje igrace kao novu sesiju.
     * 
     * @param req Request objekat koji stize kada se posalje zahtev ka serveru.
     * @param res Response objekat koji se koristi za slanje saobracaja ka igracu.
     * @throws IOException ako dodje do greske prilikom konektovanja igraca..
     */
    public void connectPlayer(final Request req, final Response res) throws IOException {
        EventResponse data = new Gson().fromJson(req.body, EventResponse.class);
        String key = generatePlayerID();
        Player player = new Player(key, data.eventParams.get("username"), data.eventParams.get("email"), res.getSocket().getInputStream(), res.getSocket().getOutputStream());
        this.playerSessions.put(player.key, player);
        this.sendConnection(res.getSocket().getOutputStream(), player.key);
        this.hook.addListener(player, this);
    }

    /**
     * Izbacuje igraca i brise njegovu sesiju i istovremeno obavestava potrebne igrace da je on napustio sesiju..
     * Ako je igrac koji se diskonektuje vlasnik lobija, taj lobi se brise..
     * Ako je igrac koji se diskonektuje zadnji igrac u sesiji ona se brise.
     * Ako je igrac koji se diskonektuje clan neke sesije tj. lobija, igraci se obavestavaju da je on napustio sesiju..
     *
     * @param player igrac koji napusta server.
     */
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

    /**
     * Izvrsava zadatai event koji je poslao igrac.
     * 
     * @param eventName     ime eventa koji se izvrsava
     * @param p             igrac koji je poslao event
     * @param eventResponse EventRepsonse objekat koji se koristi za slanje podataka ka igracu.
     */
    public void executeEvent(final String eventName, final Player p, final EventResponse eventResponse) {
        this.eventHandler.executeEvent(eventName, p, eventResponse, this);
    }

    /**
     * Generise jedinstveni ID za igraca.
     * 
     * @return vraca generisani ID.
     */
    public String generatePlayerID() {
        String key = FuseID.generateToken();
        while (playerSessions.containsKey(key)) {
            key = FuseID.generateToken();
        }
        return key;
    }

    /**
     * Salje podatke za konekciju u OutputStream od igraca..
     *
     * @param out OutputStream koji salje konekciju ka igracu.
     * @param key ID igraca.
     */
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

    /**
     * Salje podatke izabranom igracu.
     *
     * @param player Igrac kome se salju podaci.
     * @param data podaci koji se salju.
     */
    public void sendData(final Player player, final EventResponse data) {
        try {
            PrintWriter writer = new PrintWriter(player.output);
            writer.println(new Gson().toJson(data));
            writer.flush();
        } catch (Exception e) {
            System.out.println("Failed to send data");
        }
    }

    /**
     * Salje podatke svim igracima u lobiju.
     * 
     * @param lobbyID ID lobija gde se salju podaci.
     * @param data podaci koji se salju.
     */
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

    @Override
    public String toString() {
        return "SessionManager [eventHandler=" + eventHandler + ", gameSessions=" + gameSessions + ", playerSessions="
                + playerSessions + "]";
    }
}
