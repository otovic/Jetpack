package utility;

public class Tuple<T, K> {
    public final T first;
    public final K second;

    public Tuple(final T first, final K second) {
        this.first = first;
        this.second = second;
    }
}
