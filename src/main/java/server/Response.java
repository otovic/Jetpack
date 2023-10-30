package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Response {
    private Socket client;
    public Response(Socket client) {
        this.client = client;
    }
    public boolean send(String status, String file) throws Exception {
        Path filePath = Paths.get("src/main/resources/static/" + file);
        try {
            if(Files.exists(filePath)) {
                String contentType = guessContentType(filePath);
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
        clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        this.client.close();
    }

    public static void json() {

    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
}
