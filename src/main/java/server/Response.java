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
    public Response(Socket client, Request req, Route route, CORSConfig serverCors) {
        this.client = client;
        this.req = req;
        this.route = route;
        this.serverCors = serverCors;
    }

    public boolean send(String status, String file) throws Exception {
        Path filePath = Paths.get("src/main/resources/static/" + file);
        try {
            RequestMethod requestMethod = UtilityService.getRequestMethodFromString(req.method);
            if(requestMethod != route.method) {
                byte[] notFoundContent = "{name: Not Found}".getBytes();
                this.sendResponse("404", "text/html", notFoundContent);
                return false;
            }
            if (Files.exists(filePath)) {
                String contentType = guessContentType(filePath);
                System.out.println(contentType + " OVO JE CONTENT TYPE");
                this.sendResponse(status, contentType, Files.readAllBytes(filePath));
                return true;
            } else {
                byte[] notFoundContent = "{name: Not Found}".getBytes();
                this.sendResponse("404", "text/html", notFoundContent);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void sendResponse(String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = this.client.getOutputStream();

        if (route.routeSpecificCORSConfig != null) {
            String allowedMethods = UtilityService.parseAllowedMethods(route.routeSpecificCORSConfig.getAllowOrigin());
            String allowedOrigins = UtilityService.parseAllowedOrigins(route.routeSpecificCORSConfig.getAllowOrigin());
            String allowedHeaders = UtilityService.parseAllowedHeaders(route.routeSpecificCORSConfig.getAllowHeaders());

            clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
            clientOutput.write(("Content-Type: " + contentType + "\r\n").getBytes());
            clientOutput.write(("Access-Control-Allow-Origin: " + allowedOrigins + "\r\n").getBytes());
            clientOutput.write(("Access-Control-Allow-Methods: " + allowedMethods + "\r\n").getBytes());
            clientOutput.write(("Access-Control-Allow-Headers: " + allowedHeaders + "\r\n").getBytes());
            clientOutput.write(("Content-Length: " + content.length + "\r\n").getBytes());
            clientOutput.write("\r\n".getBytes());
            clientOutput.write(content);
            clientOutput.flush();
            this.client.close();

            return;
        }

        clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        clientOutput.write(("Content-Type: " + contentType + "\r\n").getBytes());
        clientOutput.write("Access-Control-Allow-Origin: *\r\n".getBytes());
        clientOutput.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n".getBytes());
        clientOutput.write("Access-Control-Allow-Headers: Content-Type\r\n".getBytes());
        clientOutput.write(("Content-Length: " + content.length + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.flush();
        this.client.close();
    }

    public static void json() {
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}
