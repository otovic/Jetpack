package server;

import event_snapshot.Snapshot;
import logger.LogType;
import logger.Logger;

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
            System.out.println("There was an error when trying to start a server on port " + this.socket + "! ERROR: " + e.getMessage());
        }
    }
    private static void handleClient(Socket client) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1].split("\\?")[0];
        Map<String, String> params = new HashMap<>();
        if(requestLine[1].split("\\?").length > 1) {
            params = URLParser.getURLparams(requestLine[1].split("\\?")[1]);
        }
        String version = requestLine[2];
        String host = requestsLines[1].split(" ")[1];

        List<String> headers = new ArrayList<>();
        Arrays.asList(requestsLines).subList(2, requestsLines.length).stream()
                .map(value -> headers.add(value));

        String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s",
                client.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);

        Path filePath = getFilePath(path);

        if (Files.exists(filePath)) {
            // file exist
            String contentType = guessContentType(filePath);
            sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
        } else {
            // 404
            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
            sendResponse(client, "404 Not Found", "text/html", notFoundContent);
        }
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
