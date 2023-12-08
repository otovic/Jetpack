package server.config;

public class ServerConfig {
    public boolean allowPermanentConnections = false;
    public int maxNumberOfConnections = 0;
    public int activeConnections = 0;
    public boolean scalable = false;

    public ServerConfig(final boolean allowPermanentConnections, final int maxNumberOfConnections, final boolean scalable) {
        this.allowPermanentConnections = allowPermanentConnections;
        this.maxNumberOfConnections = maxNumberOfConnections;
        this.scalable = scalable;
    }

    public void addConnection() {
        this.activeConnections++;
        System.out.println("(ADD) Number of connections: " + this.activeConnections);
    }

    public void removeConnection() {
        this.activeConnections--;
        System.out.println("(REMOVE) Number of connections: " + this.activeConnections);
    }

    public ServerConfig() {}
}
