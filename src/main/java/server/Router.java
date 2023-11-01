package server;

import models.Callback;
import models.RequestMethod;

import java.util.HashMap;
import java.util.Map;

public class Router {
    public HashMap<String, Route> routes = new HashMap<>();

    public void registerRoute(String route, RequestMethod method, CORSConfig corsConfig, Callback callback) {
        this.routes.put(route, new Route(route, method, corsConfig, callback));
    }

    public Map<String, String> getURLParams(String params) {
        Map<String, String> paramsMap = new HashMap<>();
        String[] paramsArray = params.split("&");

        for(String param : paramsArray) {
            String[] paramArray = param.split("=");
            paramsMap.put(paramArray[0], paramArray[1]);
        }

        return paramsMap;
    }
}
