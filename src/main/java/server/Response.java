package server;

import models.RequestMethod;
import utility.UtilityService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Response {
    private Socket client;
    private Request req;
    private Route route;
    private CORSConfig serverCors;

    private String allowedMethods;
    private String allowedOrigins;
    private String allowedHeaders;

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
            if(requestMethod != route.method) {
                this.send404("Methods do not match! Expected: " + route.method + " | Actual: " + requestMethod);
            }
            if(!serverCors.getAllowMethods().contains(req.method)) {
                this.send404("Method not allowed! Expected: " + this.allowedMethods + " | Actual: " + req.method);
            }
            if (Files.exists(filePath)) {
                String contentType = guessContentType(filePath);
                this.sendResponse(status, contentType, Files.readAllBytes(filePath));
            } else {
                this.send404("File not found!");
            }
            return true;
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
