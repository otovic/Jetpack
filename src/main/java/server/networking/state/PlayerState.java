package server.networking.state;

public class PlayerState<T> {
    public T data;

    public PlayerState(T data) {
        this.data = data;
    }
}
