package models;

import java.io.InputStream;
import java.io.OutputStream;

import server.networking.sessions.SessionManager;
import utility.json.object.JSONObject;

public interface NativeEventTask {
    void run(JSONObject data, SessionManager<?> manager, InputStream input, OutputStream output) throws Exception;
}
