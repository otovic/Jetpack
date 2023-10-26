package server;

import event_snapshot.Snapshot;
import logger.LogType;
import logger.Logger;
import logger.LoggerException;
import models.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;

public class Server {
    public int socket;
    public boolean allowClientConnections = false;
    private URLParser urlParser = new URLParser();
    private Router router = new Router();

    private Snapshot currentEvent = new Snapshot();

    public Server(int socket, boolean allowClientConnections) {
        this.socket = socket;
        this.allowClientConnections = allowClientConnections;
    }

    private void updateSnapshot(String event) {
        this.currentEvent.updateSnapshot(event);
    }
    public void startServer() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(this.socket)) {
            this.updateSnapshot("Server started on port " + this.socket + "!");
            Logger.logMessage(LogType.SERVER_START, true, this.currentEvent);
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    this.updateSnapshot("Client connected!");
                    Logger.logMessage(LogType.CLIENT_CONNECTED, true, this.currentEvent);
                    handleClient(client);
                }
            }
        } catch (Exception e) {
            this.updateSnapshot("There was an error when trying to start a server on port " + this.socket + "! ERROR: " + e.getMessage());
            Logger.logMessage(LogType.ERROR, true, this.currentEvent);
        }
    }
    private void handleClient(Socket client) throws IOException, LoggerException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        String request = requestBuilder.toString();
        Request req = parseRequest(request);
        List<String> headers = parseHeaders(request.split("\r\n"));

        this.updateSnapshot("Client Info: " + client.toString() + " | Request: " + req.toString() + " | Headers: " + headers.toString() + "");

        Logger.logRequest(req, true, this.currentEvent);

        Path filePath = getFilePath(req.path);

        if (Files.exists(filePath)) {
            String contentType = guessContentType(filePath);
            sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
        } else {
            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
            sendResponse(client, "404 Not Found", "text/html", notFoundContent);
        }
    }

    private static Request parseRequest(String req) {
        String[] reqLines = req.split("\r\n");
        String[] reqLine = reqLines[0].split(" ");
        String method = reqLine[0];
        String path = reqLine[1].split("\\?")[0];

        Map<String, String> params = new HashMap<>();
        if(reqLine[1].split("\\?").length > 1) {
            params = URLParser.getURLparams(reqLine[1].split("\\?")[1]);
        }

        String version = reqLine[2];
        String host = reqLines[1].split(" ")[1];

        List<String> headers = new ArrayList<>();
        Arrays.asList(reqLines).subList(2, reqLines.length).stream()
                .map(headers::add);

        return new Request(method, path, params, version, host, headers);
    }

    private static List<String> parseHeaders(String[] reqLines) {
        return Arrays.asList(reqLines).subList(2, reqLines.length);
    }

    public void addRoute(Route route, Callback callback) {
        this.router.registerRoute(route, callback);
    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }

        return Paths.get("/tmp/www", path);
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}
