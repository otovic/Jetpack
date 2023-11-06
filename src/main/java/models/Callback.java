package models;

import server.client.Request;
import server.client.Response;

public interface Callback {
    boolean exe(Request req, Response res) throws Exception;
}
