package server.networking.sessions;

public interface PlayerSessionEvents<T> {
    public void addPlayer(T param);
    public void removePlayer(T param);
    public void updatePlayerData(T param);
}
