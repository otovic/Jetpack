package server;

import models.Callback;

import java.util.HashMap;
import java.util.Map;

public class Router {
    public HashMap<String, Callback> routes = new HashMap<>();

    public void registerRoute(String route, Callback callback) {
        this.routes.put(route, callback);
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
