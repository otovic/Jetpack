package models;

import java.io.InputStream;
import java.io.OutputStream;

import server.networking.sessions.SessionManager;

public interface NativeEventTask {
    void run(String data, InputStream input, OutputStream output) throws Exception;
}
