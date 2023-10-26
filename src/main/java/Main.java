import server.Route;
import server.Server;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8080, false);

        server.addRoute(new Route("/", "resource/static/index.html"), ((req, res) -> {
            return res.toString();
        }));

        server.startServer();
    }
}
