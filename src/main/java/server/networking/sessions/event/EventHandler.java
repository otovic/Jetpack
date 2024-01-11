package server.networking.sessions.event;

import java.util.HashMap;

import models.Event;
import server.client.EventResponse;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;

/**
 * EventHandler klasa je zaduzena za sve evente koji su registrovani na serveru.
 * Preko ove klase se izvrsavaju eventi koji su registrovani na serveru.
 */
public class EventHandler {
    public HashMap<String, Event> events = new HashMap<>();

    /**
     * registruje event sa datim imenom.
     * Ako postoji event sa datim imenom izbacuje gresku.
     *
     * @param eventName ime eventa
     * @param event     kod koji ce se izvrsiti kada se event pozove
     */
    public void registerEvent(final String eventName, final Event event) {
        if (events.containsKey(eventName)) {
            System.out.println("Event already registered");
            return;
        }

        events.put(eventName, event);
    }

    /**
     * Izvrsava dati event sa datim parametrima.
     * Moze se desiti da event nije registrovan pa se baca Exception.
     *
     * @param eventName     Ime veenta koji treba da se izvrsi
     * @param p             igrac koji je inicirao event
     * @param eventResponse odgovor koji se salje klijentu
     * @param manager       menadzer sesija
     */
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
