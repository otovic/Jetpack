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

        server.corsConfig.setAllowOrigins(Arrays.asList("*"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.addRoute("/", RequestMethod.GET, ((req, res) -> {
            return res.send("200 OK", "index.html");
        }));

        server.addRoute("/test", RequestMethod.POST, ((req, res) -> {
            return res.send("200 OK", "index.html");
        }));

        server.startServer();
    }
}
