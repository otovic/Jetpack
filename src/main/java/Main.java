import server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main( String[] args ) throws Exception {
        Server server = new Server(8080, false);
        server.startServer();
    }
}
