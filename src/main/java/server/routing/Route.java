package server.routing;

import models.Callback;
import models.RequestMethod;
import server.config.CORSConfig;

public class Route {
    public final String path;
    public final RequestMethod method;
    public final CORSConfig routeSpecificCORSConfig;
    public final Callback callback;

    public Route(String path, RequestMethod method, CORSConfig corsConfig, Callback callback) {
        this.path = path;
        this.method = method;
        this.routeSpecificCORSConfig = corsConfig;
        this.callback = callback;
    }
}
