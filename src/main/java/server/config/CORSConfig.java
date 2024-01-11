package server.config;

import java.util.List;

/**
 * Klasa za postavljanje CORS konfiguracije.
 * Cuva se lista dozvoljenih origin-a, lista dozvoljenih metoda i lista dozvoljenih head-er-a.
 */
public class CORSConfig {
    private List<String> accessControlAllowOrigin;
    private List<String> accessControlAllowMethods;
    private List<String> accessControlAllowHeaders;

    public CORSConfig(List<String> allowOrigin, List<String> allowMethods, List<String> allowHeaders) {
        this.accessControlAllowOrigin = allowOrigin;
        this.accessControlAllowMethods = allowMethods;
        this.accessControlAllowHeaders = allowHeaders;
    }

    public List<String> getAllowOrigin() {
        return this.accessControlAllowOrigin;
    }

    public List<String> getAllowMethods() {
        return this.accessControlAllowMethods;
    }

    public List<String> getAllowHeaders() {
        return this.accessControlAllowHeaders;
    }

    public Void setAllowOrigins(List<String> allowOrigin) {
        this.accessControlAllowOrigin = allowOrigin;
        return null;
    }

    public Void setAllowMethods(List<String> allowMethods) {
        this.accessControlAllowMethods = allowMethods;
        return null;
    }

    public Void setAllowHeaders(List<String> allowHeaders) {
        this.accessControlAllowHeaders = allowHeaders;
        return null;
    }

    public CORSConfig() {

    }
}
