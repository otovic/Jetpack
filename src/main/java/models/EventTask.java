package models;

import server.networking.sessions.SessionManager;

public interface EventTask {
    void run(String data) throws Exception;
}
