package server;

import java.util.HashMap;
import java.util.Map;

public class URLParser {
    public static Map<String, String> getURLparams(String params) {
        Map<String, String> paramsMap = new HashMap<>();
        String[] paramsArray = params.split("&");

        for(String param : paramsArray) {
            String[] paramArray = param.split("=");
            paramsMap.put(paramArray[0], paramArray[1]);
        }

        return paramsMap;
    }
}
