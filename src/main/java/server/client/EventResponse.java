package server.client;

import java.util.HashMap;

public class EventResponse {
    public String eventName;
    public HashMap<String, String> eventData;
    public HashMap<String, String> eventParams;

    public EventResponse(String eventName, HashMap<String, String> eventData, HashMap<String, String> eventParams) {
        this.eventName = eventName;
        this.eventData = eventData;
        this.eventParams = eventParams;
    }

    @Override
    public String toString() {
        return "EventResponse [eventData=" + eventData + ", eventName=" + eventName + ", eventParams=" + eventParams
                + "]";
    }
}
