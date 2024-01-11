package server.networking.state;

//Ne koristi se
public class PlayerState<T> {
    public T data;

    public PlayerState(T data) {
        this.data = data;
    }
}
