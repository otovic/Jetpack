package server.multithreading;

import server.client.Request;
import server.client.Response;
import server.config.ServerConfig;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;
import utility.json.JSON;
import utility.json.object.JSONField;
import utility.json.object.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Hook {
    private int taskPool;
    private ServerConfig serverConfig;
    private List<TaskThread> threads;
    private List<Runnable> tasks;

    public Hook(final int taskPool, ServerConfig serverConfig) {
        this.taskPool = taskPool;
        this.serverConfig = serverConfig;
        this.threads = new ArrayList<>();
        this.tasks = new ArrayList<>();

        for (int i = 0; i < this.taskPool; i++) {
            TaskThread taskThread = new TaskThread();
            taskThread.start();
            this.threads.add(taskThread);
        }
    }

    public void submitTask(Runnable task) {
        synchronized (this.tasks) {
            this.tasks.add(task);
            this.tasks.notify();
        }
    }

    public void shutdown() {
        for (TaskThread thread : this.threads) {
            thread.interrupt();
        }
    }

    public void fireEvent(final Request req, final Response res, final SessionManager manager) throws IOException {
        submitTask(() -> {
            System.out.println("event task added");
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(res.getSocket().getInputStream()));
                String message = br.readLine();
                JSONObject obj = JSON.deserialize(message);
                if (message == null) {
                    System.out.println("Client disconnected.");
                }
                if (message.equals("quit")) {
                    System.out.println("Received quit message. Closing connection.");
                } else {
                    System.out.println(((JSONField) obj.fields.get(0)).field);
                    if (((JSONField) obj.fields.get(0)).field.equals("connectToServer")) {
                        Player pl = Player.generateNewPlayer(manager, message, message,
                                res.getSocket().getInputStream(), res.getSocket().getOutputStream());
                        manager.connectPlayer(pl);
                        this.addListener(pl, manager);
                    }
                    // manager.activePlayers.forEach((key, value) -> {
                    // System.out.println("POCINJEM");
                    // Player p = (Player) value;
                    // try {
                    // PrintWriter rr = new PrintWriter(p.output, true);
                    // System.out.println("Stampam");
                    // System.out.println(p.output);
                    // rr.println(JSON.repSerialize(p));
                    // System.out.println("Stampam123");
                    // } catch (Exception e) {
                    // System.out.println("GRESKA");
                    // }
                    // });
                }
            } catch (Exception e) {
                System.out.println("NECE");
                System.out.println(e.getMessage());
            }
        });
    }

    public void addListener(final Player<?> player, final SessionManager manager) throws IOException {
        submitTask(() -> {
            System.out.println("listen task added");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(player.input))) {
                while (true) {
                    String message = br.readLine();
                    if (message == null) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    if (message.equals("quit")) {
                        System.out.println("Received quit message. Closing connection.");
                        break;
                    } else {
                        System.out.println(message);
                        // manager.activePlayers.forEach((key, value) -> {
                        // System.out.println("POCINJEM");
                        // Player p = (Player) value;
                        // try {
                        // PrintWriter rr = new PrintWriter(p.output, true);
                        // System.out.println("Stampam");
                        // System.out.println(p.output);
                        // rr.println(JSON.repSerialize(p));
                        // System.out.println("Stampam123");
                        // } catch (Exception e) {
                        // System.out.println("GRESKA");
                        // }
                        // });
                    }
                }
                System.out.println("STREAM CLOSED");
            } catch (Exception e) {
                System.out.println("NECE");
                System.out.println(e.getMessage());
            }
        });
    }

    private class TaskThread extends Thread {
        public TaskThread() {
        }

        @Override
        public void run() {
            while (true) {
                Runnable task;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Thread interrupted: " + e.getMessage());
                            return;
                        }
                    }
                    task = tasks.remove(0);
                }
                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.out.println("Task failed: " + e.getMessage());
                }
            }
        }
    }
}
