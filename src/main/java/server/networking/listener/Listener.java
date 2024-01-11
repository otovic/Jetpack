package server.networking.listener;

import java.net.Socket;

import server.multithreading.Hook;

//Ne koristi se
public class Listener {
    public Socket socket;
    private Hook hook;

    public Listener(final Socket socket, final Hook hook) {
        this.socket = socket;
        this.hook = hook;
    }

    @Override
    public String toString() {
        return "Listener [hook=" + hook + ", socket=" + socket + "]";
    }
}
