package server.client;

import models.RequestMethod;
import server.config.CORSConfig;
import server.routing.Route;
import utility.UtilityService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class Response {
    private final Socket client;
    private final Request req;
    private final Route route;
    private final CORSConfig serverCors;

    private final String allowedMethods;
    private final String allowedOrigins;
    private final String allowedHeaders;

    public Response(Socket client, Request req, Route route, CORSConfig serverCors) {
        this.client = client;
        this.req = req;
        this.route = route;
        this.serverCors = serverCors;

        if(route.routeSpecificCORSConfig != null) {
            this.allowedMethods = UtilityService.parseAllowedMethods(route.routeSpecificCORSConfig.getAllowMethods());
            this.allowedOrigins = UtilityService.parseAllowedOrigins(route.routeSpecificCORSConfig.getAllowOrigin());
            this.allowedHeaders = UtilityService.parseAllowedHeaders(route.routeSpecificCORSConfig.getAllowHeaders());
        } else {
            this.allowedMethods = UtilityService.parseAllowedMethods(serverCors.getAllowMethods());
            this.allowedOrigins = UtilityService.parseAllowedOrigins(serverCors.getAllowOrigin());
            this.allowedHeaders = UtilityService.parseAllowedHeaders(serverCors.getAllowHeaders());
        }
    }

    public Socket getSocket() { return this.client; }

    public void send(String status, String file) throws Exception {
        Path filePath = Paths.get("src/main/resources/static/" + file);
        try {
            RequestMethod requestMethod = UtilityService.getRequestMethodFromString(req.method);
            assert requestMethod != null;
            if(requestMethod.equals(RequestMethod.OPTIONS)) {
                this.sendResponse("200 OK", "text/html", "".getBytes());
            }
            if(requestMethod != route.method) {
                System.out.println("Methods do not match! Expected: " + route.method + " | Actual: " + requestMethod);
                this.send404("Methods do not match! Expected: " + route.method + " | Actual: " + requestMethod);
            }
            if(!serverCors.getAllowMethods().contains(req.method)) {
                System.out.println("Method not allowed! Expected: " + this.allowedMethods + " | Actual: " + req.method);
                this.send404("Method not allowed! Expected: " + this.allowedMethods + " | Actual: " + req.method);
            }
            if (Files.exists(filePath)) {
                String contentType = guessContentType(filePath);
                this.sendResponse(status, contentType, Files.readAllBytes(filePath));
            } else {
                System.out.println("File not found!");
                this.send404("File not found!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void echo() {
        try {
            System.out.println("Printujem");
            PrintWriter writer = new PrintWriter(this.client.getOutputStream(), true);
            while(!this.client.isClosed()) {
                writer.println("Napravljen igrac!");
            }
            System.out.println("CLOSED");
        } catch (Exception e) {
            System.out.println("GRESKA PRILIKOM ODGOVORA");
        }
    }

    private void sendResponse(String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = this.client.getOutputStream();
        this.writeClientOutput(clientOutput, status, contentType, content);
    }

    private void writeClientOutput(OutputStream stream, String status, String contentType, byte[] content) throws IOException {
        stream.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        stream.write(("Content-Type: " + contentType + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Origin: " + allowedOrigins + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Methods: " + allowedMethods + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Headers: " + allowedHeaders + "\r\n").getBytes());
        stream.write(("Content-Length: " + content.length + "\r\n").getBytes());
        stream.write("\r\n".getBytes());
        stream.write(content);
        stream.flush();
        this.client.close();
    }

    private void send404(String message) throws IOException {
        try {
            byte[] notFoundContent = ("{message: " + message + "}").getBytes();
            this.sendResponse("404", "text/html", notFoundContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverBusy(OutputStream stream) throws IOException {
        stream.write(("HTTP/1.1 409 \r\n").getBytes());
        stream.write(("Access-Control-Allow-Origin: " + allowedOrigins + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Methods: " + allowedMethods + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Headers: " + allowedHeaders + "\r\n").getBytes());
        stream.write(("Content-Length: 0\r\n").getBytes());
        stream.write("\r\n".getBytes());
        stream.flush();
        this.client.close();
    }

    public void json(final Object content) throws IOException {
        OutputStream clientOutput = this.client.getOutputStream();
        Gson gson = new Gson();
        this.sendJson(clientOutput, gson.toJson(content));
    }

    public void rawjson(final Object content) throws IOException {
        OutputStream clientOutput = this.client.getOutputStream();
        PrintWriter writer = new PrintWriter(clientOutput);
        writer.println(new Gson().toJson(content));
        writer.flush();
    }

    private void sendJson(OutputStream stream, String content) throws IOException {
        stream.write(("HTTP/1.1 200 OK\r\n").getBytes());
        stream.write(("Content-Type: application/json \r\n").getBytes());
        stream.write(("Access-Control-Allow-Origin: " + allowedOrigins + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Methods: " + allowedMethods + "\r\n").getBytes());
        stream.write(("Access-Control-Allow-Headers: " + allowedHeaders + "\r\n").getBytes());
        stream.write(("Content-Length: " + content.getBytes().length + "\r\n").getBytes());
        stream.write("\r\n".getBytes());
        stream.write(content.getBytes());
        stream.flush();
        this.client.close();
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    @Override
    public String toString() {
        return "Response [client=" + client + ", req=" + req + ", route=" + route + ", serverCors=" + serverCors
                + ", allowedMethods=" + allowedMethods + ", allowedOrigins=" + allowedOrigins + ", allowedHeaders="
                + allowedHeaders + "]";
    }
}
