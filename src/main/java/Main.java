import models.RequestMethod;
import server.config.CORSConfig;
import server.networking.sessions.game.GameSession;
import server.Server;
import server.client.EventResponse;
import test_classes.Person;
import test_classes.PlayerData;
import test_classes.PlayerR;
import utility.json.JSON;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.sql.ResultSet;

import com.mysql.cj.jdbc.Driver;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8082, false, 20, 20);
        server.setDatabase("jdbc:mysql://localhost:3306/ludofx", "root", "");

        server.registerEvent("PETAR", (data, manager) -> {
            System.out.println("RADI EVENT");
        });

        server.corsConfig.setAllowOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.addRoute("/connect", ((req, res) -> {
            System.out.println("CONNECTED");
            // server.fireEvent(req, res);
        }));

        server.addRoute("/register", ((req, res) -> {
            String query = "INSERT INTO players (username, email, password) VALUES ('" + req.params.get("username") + "', '"
                    + req.params.get("email") + "', '" + req.params.get("password") + "')";
            server.database.connect();
            Boolean result = server.database.executeQuery(query);
            
            if(result) {
                res.json(new EventResponse("register", new HashMap<>() {{
                    put("result", result.toString());
                }}, new HashMap<>()));
            } else {
                res.json(new EventResponse("register", new HashMap<>() {{
                    put("result", result.toString());
                }}, new HashMap<>()));
            }
        }));

        server.addRoute("/petar", ((req, res) -> {
            res.send("200 OK", "index.html");
        }));

        CORSConfig config = new CORSConfig(Arrays.asList("*"),
                Arrays.asList("GET", "POST", "PUT", "DELETE"),
                Arrays.asList("Content-Type"));

        server.start();
    }
}
