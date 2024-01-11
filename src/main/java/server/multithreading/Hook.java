package server.multithreading;

import server.client.EventResponse;
import server.config.ServerConfig;
import server.networking.sessions.SessionManager;
import server.networking.sessions.player.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import exceptions.LogoutException;

/**
 * Hook klasa se koristi za upravljanje nitima. Koristi se za kreiranje niti koje ce izvrsavati zahteve koji stizu prema serveru
 */
public class Hook {
    private int taskPool;
    private ServerConfig serverConfig;
    private List<TaskThread> threads;
    private List<Runnable> tasks;

    /**
     * Konstrukotr klase Hook.
     * U konstruktoru se inicijalizuje odredjeni broj Threadova koji je specificiran u podesavanjima servera.
     * 
     * @param taskPool broj threadova koji ce biti napravljen.
     * @param serverConfig Objekat konfiguracije servera.
     */
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

    /**
     * Dodaje task koji treba da izvrsi server.
     *
     * @param task zadatak koji treba da bude izvrsen
     */
    public void submitTask(Runnable task) {
        synchronized (this.tasks) {
            this.tasks.add(task);
            this.tasks.notify();
        }
    }

    /**
     * Gasi sve threadove.
     */
    public void shutdown() {
        for (TaskThread thread : this.threads) {
            thread.interrupt();
        }
    }

    /**
     * Dodaje listener za sve igrace koji su ostvarili konekciju na server.
     * 
     * @param player igrac koji salje zahteve.
     * @param manager menadzer sesija.
     * @throws IOException ako dodje do greske prilikom izvrsavanja nekog zadatka i diskonektuje igraca.
     */
    public void addListener(final Player player, final SessionManager manager) throws IOException {
        submitTask(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(player.input))) {
                while (true) {
                    String message = br.readLine();
                    EventResponse eventResponse = new Gson().fromJson(message, EventResponse.class);
                    if (eventResponse.eventName == null || eventResponse.eventName.equals("")) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    if (eventResponse.eventName.equals("quit")) {
                        System.out.println("Received quit message. Closing connection.");
                        break;
                    } else {
                        manager.executeEvent(eventResponse.eventName, player, eventResponse);
                        if (eventResponse.eventName.equals("logout")) {
                            throw new LogoutException("LOGOUT");
                        }
                    }
                }
                System.out.println("STREAM CLOSED");
            } catch (LogoutException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                manager.disconnectPlayer(player);
            }
        });
    }

    /**
         * Ceka da se doda zadatak u listu pa se taj isti zadatak dodeljuje jednom thread-u na izvrsavanje.
         */
        private class TaskThread extends Thread {
            public TaskThread() {
            }

            /**
             * Izvrsava taskove iz taska.
             * ako nema zadataka za izvrsavanje, svi threadovi se stavljaju na cekanje.
             * ako neki zadatak ne uspe baca se greska.
             */
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
