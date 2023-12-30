package server.networking.sessions.event;

import java.io.IOException;

import com.google.gson.Gson;

import server.client.EventResponse;
import server.client.Request;
import server.client.Response;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;

public class EventController {
    public Player player;
    public EventResponse data;
    private SessionManager sessionManager;

    public EventController(final Player player, final EventResponse eventResponse, final SessionManager sessionManager) {
        this.player = player;
        this.data = eventResponse;
        this.sessionManager = sessionManager;
    }

    private String generatePlayerID() {
        return sessionManager.generatePlayerID();
    }
}
