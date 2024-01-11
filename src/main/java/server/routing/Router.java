package server.routing;

import models.Callback;
import models.RequestMethod;
import server.config.CORSConfig;

import java.util.HashMap;
import java.util.Map;

public class Router {
    public HashMap<String, Route> routes = new HashMap<>();

    /**
     * Dodaje rutu u mapu ruta.
     *
     * @param route       putanja.
     * @param method      metoda zahteva, get, post, put.
     * @param corsConfig  CORS konfiguracija za rutu.
     * @param callback    kod koji ce se izvrsiti kada se pristupi ovoj ruti.
     */
    public void registerRoute(String route, RequestMethod method, CORSConfig corsConfig, Callback callback) {
        this.routes.put(route, new Route(route, method, corsConfig, callback));
    }

    /**
     * Parsira URL parametre i vraca mapu parametara
     *
     * @param params URL parametri
     */
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
