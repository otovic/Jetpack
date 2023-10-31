import client.ParamsRouter;
import server.Route;
import server.Server;
import test_classes.Person;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8080, false);

        server.addRoute("/", ((req, res) -> {
            Person person = ParamsRouter.routeFromParams(req.params, Person.class);
            return res.send("200 OK", "index.html");
        }));

        server.addRoute("/test", ((req, res) -> {
            Person p = ParamsRouter.routeFromParams(req.params, Person.class);
            System.out.println(p.age + " " + p.name + " " + p.surname + " " + p.address);
            return res.send("200 OK", "index.html");
        }));

        server.startServer();
    }
}
