package server.networking.sessions.event;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

import models.Event;
import server.client.EventResponse;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;

public class EventHandler {
    public HashMap<String, Event> events = new HashMap<>();

    public void registerEvent(final String eventName, final Event event) {
        if (events.containsKey(eventName)) {
            System.out.println("Event already registered");
            return;
        }

        events.put(eventName, event);
    }

    public void executeEvent(final String eventName, final Player p, final EventResponse eventResponse,
            final SessionManager manager) {
        if (!events.containsKey(eventName)) {
            System.out.println("Event not registered");
            return;
        }

        try {
            EventController eventController = new EventController(p, eventResponse, manager);
            events.get(eventName).call(eventController);
        } catch (Exception e) {
            System.out.println("Event failed to run: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "EventHandler [events=" + events + "]";
    }
}
