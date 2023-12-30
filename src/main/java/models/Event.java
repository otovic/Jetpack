package models;

import server.networking.sessions.event.EventController;

public interface Event {
    void call(EventController controller);
}
