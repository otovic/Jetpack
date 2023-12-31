import models.RequestMethod;
import server.config.CORSConfig;
import server.networking.sessions.game.GameSession;
import server.Server;
import server.client.EventResponse;
import test_classes.Person;
import test_classes.PlayerData;
import test_classes.PlayerR;
import utility.json.JSON;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.sql.ResultSet;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.Driver;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8082, false, 20, 20);
        server.setDatabase("jdbc:mysql://localhost:3306/ludofx", "root", "");

        server.corsConfig.setAllowOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.registerEvent("logout", (controller) -> {
            controller.logout();
        });

        server.addRoute("/connect", ((req, res) -> {
            
        }));

        server.addRoute("/register", RequestMethod.POST, ((req, res) -> {
            EventResponse eventResponse = new Gson().fromJson(req.body, EventResponse.class);
            String query = "INSERT INTO players (username, email, password) VALUES ('" + eventResponse.eventParams.get("username") + "', '"
                    + eventResponse.eventParams.get("email") + "', '" + eventResponse.eventParams.get("password") + "')";
            server.database.connect();
            String result = server.database.executeQuery(query);
            
            if(result.equals("Success")) {
                server.connectClient(req, res);
            } else {
                res.rawjson(new EventResponse("registerPlayer", new HashMap<>() {{
                    put("error", result);
                }}, new HashMap<>()));
            } 
        }));

        server.addRoute("/login", RequestMethod.POST, ((req, res) -> {
            System.out.println("Login route");
            EventResponse eventResponse = new Gson().fromJson(req.body, EventResponse.class);
            String query = "SELECT * FROM players WHERE username = '" + eventResponse.eventParams.get("username") + "' AND password = '" + eventResponse.eventParams.get("password") + "'";
            server.database.connect();
            System.out.println(query);
            ResultSet result = server.database.executeQueryWithResult(query);
            if(result.next()) {
                server.connectClient(req, res);
            } else {
                res.rawjson(new EventResponse("loginPlayer", new HashMap<>() {{
                    put("error", "Invalid credentials");
                }}, new HashMap<>()));
            }
        }));


        // server.addRoute("/petar", ((req, res) -> {
        //     res.send("200 OK", "index.html");
        // }));

        server.start();
    }
}
