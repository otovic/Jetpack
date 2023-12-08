package server.multithreading;
import server.config.ServerConfig;

import java.util.LinkedList;
import java.util.List;

public class Hook {
    private int taskPool;
    private ServerConfig serverConfig;
    private List<TaskThread> threads;
    private List<Runnable> tasks;

    public Hook(final int taskPool, ServerConfig serverConfig) {
        this.taskPool = taskPool;
        this.serverConfig = serverConfig;
        this.threads = new LinkedList<>();
        this.tasks = new LinkedList<>();

        for (int i = 0; i < this.taskPool; i++) {
            TaskThread taskThread = new TaskThread();
            taskThread.start();
            this.threads.add(new TaskThread());
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

    private class TaskThread extends Thread {
        public TaskThread() {}
    
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
