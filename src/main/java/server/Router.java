package server;

import java.util.HashMap;

public class Router {
    public HashMap<String, Void> routes;

    public void registerRoute(String route) {
        this.routes.put(route, null);
    }
}
