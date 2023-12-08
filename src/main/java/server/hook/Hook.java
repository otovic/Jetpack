package server.hook;
import models.HookCallback;

import java.net.Socket;

public class Hook extends Thread {
    private Socket socket;
    private HookCallback callback;
    public Hook(Socket socket, HookCallback callback) {
        this.socket = socket;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            super.run();
            this.callback.call();
            Thread.sleep(10000);
            System.out.println("Closing socket...");
        } catch (Exception e) {
            System.out.println("Error closing socket: " + e.getMessage());
        } finally {
            try {
                this.socket.close();
                System.out.println("Socket closed!");
            } catch (Exception e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
