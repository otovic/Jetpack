package server.networking.sessions.game;

/**
 * Prestavlja jedan dogadjaj u igri koji treba svi igraci da zavrse kako bi se igra nastavila. (Kao npr. bacanje kocke, pomeranje figure moze da se izvrsi nakon sto se na svakom klijentu zavrsi bacanje kockice)
 */
public class Move {
    public int playerFinished;
    public String owningPlayer;
    public String data;

    /**
     * Konstruktor za klasu.
     * 
     * @param playerFinished koliko igraca je zavrsilo ovaj dogadjaj.
     * @param owningPlayer Igrac koji je inicirao potez.
     * @param data Podaci o potezu kao koj broj kockice je bacen i slicno.
     */
    public Move(int playerFinished, String owningPlayer, String data) {
        this.playerFinished = playerFinished;
        this.owningPlayer = owningPlayer;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Move [data=" + data + ", owningPlayer=" + owningPlayer + ", playerFinished=" + playerFinished + "]";
    }
}
