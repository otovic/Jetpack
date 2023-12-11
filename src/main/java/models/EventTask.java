package models;

import server.networking.sessions.SessionManager;
import utility.json.object.JSONObject;

public interface EventTask {
    void run(JSONObject data, SessionManager<?> manager) throws Exception;
}
