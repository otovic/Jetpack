package server;

import java.util.List;
import java.util.Map;

public class Request {
    public String method;
    public String path;
    public Map<String, String> params;
    public String version;
    public String host;

    public String body;

    public Request(String method, String path, Map<String, String> params, String version, String host, List<String> headers) {
        this.method = method;
        this.path = path;
        this.params = params;
        this.version = version;
        this.host = host;
    }
}
