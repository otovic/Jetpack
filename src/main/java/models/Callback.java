package models;

import server.client.Request;
import server.client.Response;

public interface Callback {
    void exe(Request req, Response res) throws Exception;
}
