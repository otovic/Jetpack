package test_classes;

import java.util.HashMap;

import models.ParamKey;

public class RequestR {
    @ParamKey(field = "event")
    public String event = "";
    @ParamKey(field = "eventData")
    public HashMap<String, String> eventData = new HashMap<>();
    @ParamKey(field = "data")
    public PlayerData data;

    public RequestR(String event, HashMap<String, String> eventData, PlayerData data) {
        this.eventData = eventData;
        this.event = event;
        this.data = data;
    }
}
