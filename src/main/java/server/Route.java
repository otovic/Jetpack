package server;

import models.Callback;
import models.RequestMethod;

public class Route {
    public String path;
    public RequestMethod method;
    public CORSConfig routeSpecificCORSConfig;
    public Callback callback;

    public Route(String path, RequestMethod method, CORSConfig corsConfig, Callback callback) {
        this.path = path;
        this.method = method;
        this.routeSpecificCORSConfig = corsConfig;
        this.callback = callback;
    }
}
