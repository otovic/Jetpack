package server.config;

/**
 * Konfiguraciona klasa za server.
 */
public class ServerConfig {
    public boolean allowPermanentConnections = false;
    public int maxNumberOfConnections = 0;
    public int activeConnections = 0;
    public boolean scalable = false;

    /**
     * Konstruktor za ServerConfig klasu.
     * 
     * @param allowPermanentConnections Da li serve dozvoljava stalnu konekciju
     * @param maxNumberOfConnections maksimalan broj onekcija.
     * @param scalable da li je server skalabilan.
     */
    public ServerConfig(final boolean allowPermanentConnections, final int maxNumberOfConnections, final boolean scalable) {
        this.allowPermanentConnections = allowPermanentConnections;
        this.maxNumberOfConnections = maxNumberOfConnections;
        this.scalable = scalable;
    }
    
    public ServerConfig() {}
}
