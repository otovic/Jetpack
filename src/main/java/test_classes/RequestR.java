package test_classes;

import java.util.HashMap;

public class RequestR<T> {
    public String event;
    public HashMap<String, String> eventData;
    public T data;

    public RequestR(String event, HashMap<String, String> eventData, T data) {
        this.eventData = eventData;
        this.event = event;
        this.data = data;
    }
}
