package models;

import server.Request;
import server.Response;

public interface Callback {
    boolean exe(Request req, Response res) throws Exception;
}
