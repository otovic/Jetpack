package models;

import server.Request;
import server.Response;

public interface Callback {
    String callback(Request req, Response res);
}
