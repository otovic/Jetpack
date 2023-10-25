import logger.Logger;
import server.Request;
import server.Server;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8080, false);
        server.startServer();
    }
}
