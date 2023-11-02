import client.ParamsRouter;
import models.RequestMethod;
import server.CORSConfig;
import server.Route;
import server.Server;
import test_classes.Person;

import java.util.Arrays;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8080, false);

        server.corsConfig.setAllowOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.addRoute("/", RequestMethod.GET, ((req, res) -> {
            return res.send("200 OK", "index.html");
        }));

        CORSConfig config = new CORSConfig(Arrays.asList("*"),
                Arrays.asList("GET", "POST", "PUT", "DELETE"),
                Arrays.asList("Content-Type"));

        server.addRoute("/test", RequestMethod.GET, config, ((req, res) -> {
            return res.send("200 OK", "index.html");
        }));

        server.startServer();
    }
}
