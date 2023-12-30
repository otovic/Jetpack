package server.networking.sessions.event;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import models.EventTask;
import models.NativeEventTask;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;
import server.networking.state.PlayerState;
import test_classes.PlayerData;
import test_classes.PlayerR;
import test_classes.RequestR;

public class EventHandler<PS, GS> {
    // public SessionManager<PS, GS> sessionManager;
    // public HashMap<String, EventTask> events = new HashMap<>();
    // public HashMap<String, NativeEventTask> nativeEvents = new HashMap<>();

    // public EventHandler(SessionManager<PS, GS> sessionManager) {
    //     this.sessionManager = sessionManager;
    // }

    // public void fireEvent(final String eventName, final String data) {
    //     if (!events.containsKey(eventName) && !nativeEvents.containsKey(eventName)) {
    //         System.out.println("Event not registered");
    //     }

    //     try {
    //         events.get(eventName).run(data, sessionManager);
    //     } catch (Exception e) {
    //         System.out.println("Event failed to run" + e.getMessage());
    //     }
    // }

    // public boolean fireNativeEvent(final String eventName, final String data, final InputStream input,
    //         OutputStream output) {
    //     if (!nativeEvents.containsKey(eventName)) {
    //         throw new RuntimeException("Event not registered");
    //     }

    //     try {
    //         nativeEvents.get(eventName).run(data, sessionManager, input, output);
    //     } catch (Exception e) {
    //         System.out.println(" NATIVE Event failed to run" + e.getMessage());
    //         return false;
    //     }

    //     return true;
    // }

    // public void addDefaultEvents() {
    //     this.registerNativeEvent("connectToServer", ((data, sessionManager, input, output) -> {
    //         Gson gson = new Gson();
    //         TypeToken<RequestR> typeToken = new TypeToken<RequestR>() {
    //         };
    //         System.out.println(typeToken.getType());
    //         RequestR re = gson.fromJson(data, typeToken.getType());
    //         System.out.println(re.eventData.get("email"));
    //         Player<PlayerData> pl = Player.generateNewPlayer(sessionManager, re.eventData.get("email"),
    //                 (PlayerData) re.data,
    //                 input, output);
    //         System.out.println("POCINJEM");
    //         sessionManager.connectPlayer(pl);
    //         RequestR rez = new RequestR("connectionSuccess", new HashMap<String, String>(), pl.state);
    //         rez.eventData.put("token", pl.key);
    //         PrintWriter pw = new PrintWriter(output, true);
    //         pw.println(gson.toJson(rez));
    //     }));

    //     this.registerEvent("onRepPlayer", ((data, sessionManager) -> {
    //         Gson gson = new Gson();
    //         TypeToken<RequestR> typeToken = new TypeToken<RequestR>() {
    //         };
    //         RequestR re = gson.fromJson(data, typeToken.getType());
    //         this.sessionManager.updatePlayerData(re.eventData.get("token"), (PS) re.data);
    //         System.out.println("UPDATED: " + re.data.point);

    //         for (Player<?> player : sessionManager.activePlayers.values()) {
    //             if (!player.key.equals(re.eventData.get("token"))) {
    //                 try {
    //                     PrintWriter rr = new PrintWriter(player.output, true);
    //                     System.out.println("Stampam");
    //                     RequestR rez = new RequestR("onRepPlayer", new HashMap<String, String>(),
    //                             (PlayerData) re.data);
    //                     rez.eventData.put("token", player.key);
    //                     rr.println(gson.toJson(rez));
    //                     System.out.println("Stampam123");
    //                 } catch (Exception e) {
    //                     System.out.println("GRESKA");
    //                 }
    //             }
    //         }
    //     }));
    // }

    // public void registerEvent(final String eventName, final EventTask event) {
    //     if (events.containsKey(eventName)) {
    //         throw new RuntimeException("Event already registered");
    //     }

    //     if (nativeEvents.containsKey(eventName)) {
    //         throw new RuntimeException("Event already registered");
    //     }

    //     events.put(eventName, event);
    // }

    // public void registerNativeEvent(final String eventName, final NativeEventTask event) {
    //     if (events.containsKey(eventName)) {
    //         throw new RuntimeException("Event already registered");
    //     }

    //     if (nativeEvents.containsKey(eventName)) {
    //         throw new RuntimeException("Event already registered");
    //     }

    //     nativeEvents.put(eventName, event);
    // }
}
