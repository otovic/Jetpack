package server;

import models.Callback;

import java.util.HashMap;
import java.util.Map;

public class Router {
    public HashMap<String, Callback> routes = new HashMap<>();

    public void registerRoute(String route, Callback callback) {
        this.routes.put(route, callback);
    }
}
