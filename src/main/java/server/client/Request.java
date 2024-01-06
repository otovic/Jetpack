package server.client;

import java.util.List;
import java.util.Map;

public class Request {
    public String method;
    public String path;
    public Map<String, String> params;
    public String version;
    public String host;
    public String body;
    public List<String> headers;

    public Request(String method, String path, Map<String, String> params, String version, String host, List<String> headers) {
        this.method = method;
        this.path = path;
        this.params = params;
        this.version = version;
        this.host = host;
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "Request [body=" + body + ", headers=" + headers + ", host=" + host + ", method=" + method + ", params="
                + params + ", path=" + path + ", version=" + version + "]";
    }
}
