import models.RequestMethod;
import server.config.CORSConfig;
import server.networking.sessions.SessionManager;
import server.Server;
import test_classes.Person;
import utility.json.JSON;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8082, false, 20, 20, new SessionManager<Integer, Integer>());

        server.corsConfig.setAllowOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        server.corsConfig.setAllowMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        server.corsConfig.setAllowHeaders(Arrays.asList("Content-Type"));

        server.addRoute("/", ((req, res) -> {  
            server.registerNewPlayer(Integer.class, "Petar", "oto@gmail.com", res);
            res.echo();
        }));

        CORSConfig config = new CORSConfig(Arrays.asList("*"),
                Arrays.asList("GET", "POST", "PUT", "DELETE"),
                Arrays.asList("Content-Type"));

        server.addRoute("/test", RequestMethod.POST, config, ((req, res) -> {
            System.out.println(req.body);
            List<Person> per = JSON.routeFromBody(req.body, Person.class);
            System.out.println(per.get(0).address);
//            for(StringBuilder param : bodyParams) {
//                Person p = JSON.toObject(param, Person.class);
//                System.out.println(p.address);
//            }
            res.send("200 OK", "index.html");
        }));

        server.start();
    }
}
