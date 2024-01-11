package server.networking.sessions.event;

import server.client.EventResponse;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;

/**
 * EventController predstavlja kontroler koji prosledjuje podatke eventu sa kojima moze da se radi (Pogledati main metodu pa server.registerEvent).
 */
public class EventController {
    public Player player;
    public EventResponse data;
    public SessionManager sessionManager;

    /**
     * Konstruktor za EventController klasu.
     * @param player igrac koji je inicirao event.
     * @param eventResponse EventResponse objekat koji sadrzi podatke koji su stigli uz zahtev.
     * @param sessionManager SessionManager koji sluzi za upravljanje sesijama.
     */
    public EventController(final Player player, final EventResponse eventResponse, final SessionManager sessionManager) {
        this.player = player;
        this.data = eventResponse;
        this.sessionManager = sessionManager;
    }

    /**
     * Diskonektuje igraca sa servera.
     */
    public void logout() {
        this.sessionManager.disconnectPlayer(this.player);
    }

    @Override
    public String toString() {
        return "EventController [data=" + data + ", player=" + player + ", sessionManager=" + sessionManager + "]";
    }
}
