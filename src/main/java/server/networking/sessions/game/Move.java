package server.networking.sessions.game;

public class Move {
    public int playerFinished;
    public String owningPlayer;
    public String data;

    public Move(int playerFinished, String owningPlayer, String data) {
        this.playerFinished = playerFinished;
        this.owningPlayer = owningPlayer;
        this.data = data;
    }
}
