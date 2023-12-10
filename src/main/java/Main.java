import models.RequestMethod;
import server.config.CORSConfig;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;
import server.Server;
import test_classes.Person;
import test_classes.PlayerR;
import test_classes.RequestR;
import utility.json.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8082, false, 20, 20);
        server.setGamingDataTypes(PlayerR.class);

        server.corsConfig.setAllowOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.addRoute("/", ((req, res) -> {
            System.out.println("index route");
            server.fireEvent(req, res);
        }));

        server.addRoute("/petar", ((req, res) -> {
            res.send("200 OK", "index.html");
        }));

        CORSConfig config = new CORSConfig(Arrays.asList("*"),
                Arrays.asList("GET", "POST", "PUT", "DELETE"),
                Arrays.asList("Content-Type"));

        server.addRoute("/test", RequestMethod.POST, config, ((req, res) -> {
            List<Person> per = JSON.routeFromBody(req.body, Person.class);
            System.out.println(per.get(0).address);
            res.send("200 OK", "index.html");
        }));

        server.start();
    }
}
