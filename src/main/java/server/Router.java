package server;

import models.Callback;

import java.util.HashMap;
import java.util.Map;

public class Router {
    public HashMap<Route, Callback> routes;

    public void registerRoute(Route route, Callback callback) {
        this.routes.put(route, callback);
    }
}
