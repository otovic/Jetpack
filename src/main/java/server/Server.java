package server;

import event_snapshot.Snapshot;
import logger.LogType;
import logger.Logger;
import exceptions.LoggerException;
import models.Callback;
import models.Event;
import models.EventTask;
import models.RequestMethod;
import server.client.EventResponse;
import server.client.Request;
import server.client.Response;
import server.config.CORSConfig;
import server.config.ServerConfig;
import server.database.Database;
import server.multithreading.Hook;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;
import server.routing.Router;
import test_classes.Person;
import test_classes.PlayerR;
import utility.json.JSON;

import java.beans.EventHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import com.google.gson.Gson;

public class Server {
    public int socket;
    private Router router = new Router();
    public CORSConfig corsConfig = new CORSConfig();
    private Snapshot currentEvent = new Snapshot();
    private ServerConfig serverConfig = new ServerConfig();
    private Hook hook;
    private SessionManager manager;

    public Database database;

    public Server(int socket, boolean allowClientConnections, int maxNumberOfConnections, int maxNumberOfThreads) {
        this.socket = socket;
        this.serverConfig = new ServerConfig(allowClientConnections, maxNumberOfConnections, false);
        this.hook = new Hook(maxNumberOfThreads, this.serverConfig);
        this.manager = new SessionManager(this.hook);
    }

    public void setDatabase(final String url, final String username, final String password) {
        this.database = new Database(url, username, password);
    }

    private void updateSnapshot(String event) {
        this.currentEvent.updateSnapshot(event);
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(this.socket)) {
            this.updateSnapshot("Server started on port " + this.socket + "!");
            Logger.logMessage(LogType.SERVER_START, true, this.currentEvent);
            while (true) {
                try {
                    // Socket client = serverSocket.accept();
                    // BufferedReader br = new BufferedReader(new
                    // InputStreamReader(client.getInputStream()));
                    // PrintWriter pr = new PrintWriter(client.getOutputStream(), true);
                    // while (true) {
                    // System.out.println(br.readLine());
                    // pr.println("ODGOVOR SA SERVERA");
                    // }
                    Socket client = serverSocket.accept();
                    hook.submitTask(() -> {
                        try {
                            this.handleIncomingRequest(client);
                        } catch (Exception e) {
                            this.updateSnapshot("There was an error when trying to handle an incoming request! ERROR: "
                                    + e.getMessage());
                            try {
                                Logger.logMessage(LogType.ERROR, true, this.currentEvent);
                            } catch (LoggerException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    this.updateSnapshot("There was an error when trying to start a server on port " + this.socket
                            + "! ERROR: " + e.getMessage());
                    Logger.logMessage(LogType.ERROR, true, this.currentEvent);
                }
            }
        } catch (Exception e) {
            this.updateSnapshot("There was an error when trying to start a server on port " + this.socket + "! ERROR: "
                    + e.getMessage());
            Logger.logMessage(LogType.ERROR, true, this.currentEvent);
        }
    }

    private void handleIncomingRequest(Socket client) throws IOException, LoggerException {
        try {
            System.out.println("Handling request from client: " + client.toString());
            final BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            final StringBuilder requestBuilder = this.buildRequest(br);
            final Request req = parseRequest(requestBuilder.toString());

            this.updateSnapshot("Client Info: " + client.toString() + " | Request: " + req.toString() + " | Headers: "
                    + req.headers.toString() + "");
            Logger.logRequest(req, true, this.currentEvent);
            this.router.routes.forEach((routePath, route) -> {
                if (routePath.equals(req.path)) {
                    try {
                        if (req.method.equals("OPTIONS")) {
                            Response res = new Response(client, req, route, this.corsConfig);
                            try {
                                res.send("200 OK", "");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        if (req.method.equals("POST") || req.method.equals("PUT")) {
                            if (req.headers.contains("Content-Length: 0")) {
                                req.body = "";
                            } else {
                                req.body = this.parseBody(req.headers, br).toString();
                            }
                        } else {
                            req.body = "";
                        }
                        route.callback.exe(req, new Response(client, req, route, this.corsConfig));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private StringBuilder parseBody(List<String> headers, BufferedReader br) throws IOException {
        int contentLength = Integer.parseInt(headers.stream()
                .filter(header -> header.contains("Content-Length: "))
                .findFirst()
                .orElse("Content-Length: 0")
                .split(" ")[1]);

        char[] bodyData = new char[contentLength];
        br.read(bodyData);

        return new StringBuilder(new String(bodyData));
    }

    private StringBuilder buildRequest(BufferedReader br) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }
        return requestBuilder;
    }

    private Request parseRequest(String req) {
        String[] reqLines = req.split("\r\n");
        String[] reqLine = reqLines[0].split(" ");

        String method = reqLine[0];
        String path = reqLine[1].split("\\?")[0];
        String version = reqLine[2];
        String host = reqLines[1].split(" ")[1];
        Map<String, String> params = new HashMap<>();

        if (reqLine[1].split("\\?").length > 1) {
            params = this.router.getURLParams(reqLine[1].split("\\?")[1]);
        }

        List<String> headers = new ArrayList<>();
        Arrays.asList(reqLines).subList(2, reqLines.length).stream().forEach(line -> {
            headers.add(line);
        });

        return new Request(method, path, params, version, host, headers);
    }

    public void addRoute(String route, RequestMethod reqMethod, CORSConfig routeCORS, Callback callback) {
        this.router.registerRoute(route, reqMethod, routeCORS, callback);
    }

    public void addRoute(String route, RequestMethod reqMethod, Callback callback) {
        this.router.registerRoute(route, reqMethod, null, callback);
    }

    public void addRoute(String route, Callback callback) {
        this.router.registerRoute(route, RequestMethod.GET, null, callback);
    }

    public void registerEvent(final String eventName, final Event event) {
        this.manager.registerEvent(eventName, event);
    }

    // public void executeEvent(final Request req, final Response res) {
    //     EventResponse eventResponse = new Gson().fromJson(req.body, EventResponse.class);
    //     this.manager.executeEvent(eventResponse.eventName, eventResponse);
    // }

    public void connectClient(final Request req, final Response res) throws IOException {
        this.manager.connectPlayer(req, res);
    }

    public void listen(final Request req, final Response res) throws IOException {
        try {
            this.hook.fireEvent(req, res, this.manager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}