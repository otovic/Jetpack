package utility.json;

import java.util.List;

public class JSON {
    public static <T> List<T> toList(final String json, final Class<T> object) {
        StringBuilder jsonBuilder = new StringBuilder(json);
        int startPosition = jsonBuilder.indexOf("[");
        int lastPosition = jsonBuilder.lastIndexOf("]");
        System.out.println(jsonBuilder.substring(startPosition, lastPosition - 1));
        return null;
    }
}
