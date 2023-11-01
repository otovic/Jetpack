package utility;

import models.RequestMethod;

import java.time.LocalTime;
import java.util.List;

public class UtilityService {
    public static String parseTimeForLog(LocalTime time) {
        StringBuilder parsedTime = new StringBuilder();
        parsedTime.append(time.getHour());
        parsedTime.append(":");
        parsedTime.append(time.getMinute());
        parsedTime.append(":");
        parsedTime.append(time.getSecond());

        return parsedTime.toString();
    }

    public static String parseAllowedMethods(List<String> allowedMethods) {
        StringBuilder parsedMethods = new StringBuilder();
        for (String method : allowedMethods) {
            parsedMethods.append(method);
            parsedMethods.append(", ");
        }
        parsedMethods.delete(parsedMethods.length() - 2, parsedMethods.length());
        return parsedMethods.toString();
    }

    public static String parseAllowedHeaders(List<String> allowedHeaders) {
        StringBuilder parsedHeaders = new StringBuilder();
        for (String header : allowedHeaders) {
            parsedHeaders.append(header);
            parsedHeaders.append(", ");
        }
        parsedHeaders.delete(parsedHeaders.length() - 2, parsedHeaders.length());
        return parsedHeaders.toString();
    }

    public static String parseAllowedOrigins(List<String> allowedOrigins) {
        StringBuilder parsedOrigins = new StringBuilder();
        for (String origin : allowedOrigins) {
            parsedOrigins.append(origin);
            parsedOrigins.append(", ");
        }
        parsedOrigins.delete(parsedOrigins.length() - 2, parsedOrigins.length());
        return parsedOrigins.toString();
    }

    public static RequestMethod getRequestMethodFromString(String method) {
        switch (method) {
            case "GET":
                return RequestMethod.GET;
            case "POST":
                return RequestMethod.POST;
            case "PUT":
                return RequestMethod.PUT;
            case "DELETE":
                return RequestMethod.DELETE;
            case "PATCH":
                return RequestMethod.PATCH;
            case "OPTIONS":
                return RequestMethod.OPTIONS;
            case "HEAD":
                return RequestMethod.HEAD;
            case "CONNECT":
                return RequestMethod.CONNECT;
            case "TRACE":
                return RequestMethod.TRACE;
            default:
                return null;
        }
    }
}
