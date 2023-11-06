package server.client;

import models.RequestMethod;
import server.config.CORSConfig;
import server.routing.Route;
import utility.UtilityService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public boolean send(String status, String file) throws Exception {
        Path filePath = Paths.get("src/main/resources/static/" + file);
        try {
            RequestMethod requestMethod = UtilityService.getRequestMethodFromString(req.method);
            assert requestMethod != null;
            if(requestMethod.equals(RequestMethod.OPTIONS)) {
                this.sendResponse("200 OK", "text/html", "".getBytes());
                return true;
            }
            if(requestMethod != route.method) {
                System.out.println("Methods do not match! Expected: " + route.method + " | Actual: " + requestMethod);
                this.send404("Methods do not match! Expected: " + route.method + " | Actual: " + requestMethod);
                return false;
            }
            if(!serverCors.getAllowMethods().contains(req.method)) {
                System.out.println("Method not allowed! Expected: " + this.allowedMethods + " | Actual: " + req.method);
                this.send404("Method not allowed! Expected: " + this.allowedMethods + " | Actual: " + req.method);
                return false;
            }
            if (Files.exists(filePath)) {
                String contentType = guessContentType(filePath);
                this.sendResponse(status, contentType, Files.readAllBytes(filePath));
                return true;
            } else {
                System.out.println("File not found!");
                this.send404("File not found!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private void sendResponse(String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = this.client.getOutputStream();
        this.writeClientOutput(clientOutput, status, contentType, content);
    }

    private void writeClientOutput(OutputStream stream, String status, String contentType, byte[] content) throws IOException {
        System.out.println(status);
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

    public static void json() {
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}