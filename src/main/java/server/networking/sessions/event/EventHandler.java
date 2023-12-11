package server.networking.sessions.event;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import models.EventTask;
import models.NativeEventTask;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;
import utility.json.object.JSONObject;

public class EventHandler<PS> {
    public SessionManager<PS> sessionManager;
    public HashMap<String, EventTask> events = new HashMap<>();
    public HashMap<String, NativeEventTask> nativeEvents = new HashMap<>();

    public EventHandler(SessionManager<PS> sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void fireEvent(final String eventName, final JSONObject data) {
        if (!events.containsKey(eventName) && !nativeEvents.containsKey(eventName)) {
            System.out.println("Event not registered");
        }

        try {
            events.get(eventName).run(data, sessionManager);
        } catch (Exception e) {
            System.out.println("Event failed to run" + e.getMessage());
        }
    }

    public boolean fireNativeEvent(final String eventName, final JSONObject data, final InputStream input, OutputStream output) {
        if (!nativeEvents.containsKey(eventName)) {
            throw new RuntimeException("Event not registered");
        }

        try {
            nativeEvents.get(eventName).run(data, sessionManager, input, output);
        } catch (Exception e) {
            System.out.println(" NATIVE Event failed to run" + e.getMessage());
            return false;
        }

        return true;
    }

    public void addDefaultEvents() {
        this.registerNativeEvent("connectToServer", ((data, sessionManager, input, output) -> {
            Player<PS> pl = Player.generateNewPlayer(sessionManager, "petar", "otovicpetar1998@gmail.com",
                    input, output);
            PrintWriter pw = new PrintWriter(output);
            pw.println(pl.key);
            System.out.println("Sending key: " + pl.key);
            sessionManager.connectPlayer(pl);
        }));
    }

    public void registerEvent(final String eventName, final EventTask event) {
        if (events.containsKey(eventName)) {
            throw new RuntimeException("Event already registered");
        }

        if (nativeEvents.containsKey(eventName)) {
            throw new RuntimeException("Event already registered");
        }

        events.put(eventName, event);
    }

    public void registerNativeEvent(final String eventName, final NativeEventTask event) {
        if (events.containsKey(eventName)) {
            throw new RuntimeException("Event already registered");
        }

        if (nativeEvents.containsKey(eventName)) {
            throw new RuntimeException("Event already registered");
        }

        nativeEvents.put(eventName, event);
    }
}
