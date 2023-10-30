import server.Route;
import server.Server;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8080, false);

        server.addRoute("/", ((req, res) -> {
            System.out.println("POZVANO JA EJE E");
            return res.send("200 OK", "index.html");
        }));

        server.startServer();
    }
}
