package server.networking.sessions;

public interface PlayerSessionEvents<T> {
    public void connectPlayer(T param);
    public void removePlayer(T param);
    public void updatePlayerData(T param);
}
