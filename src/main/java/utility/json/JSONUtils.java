package utility.json;

import utility.Tuple;
import utility.json.object.JSONFieldType;

public class JSONUtils {
    public static boolean isArray(final String body) {
        return body.indexOf("[") == 0;
    }

    public static StringBuilder removeObjectBrackets(final StringBuilder object) {
        object.deleteCharAt(object.indexOf("{"));
        object.deleteCharAt(object.lastIndexOf("}"));
        return object;
    }

    public static Tuple getFieldLengthAndType(final StringBuilder object) {
        if (object.toString().equals("")) return null;
        int objectSplitPosition = object.indexOf(":");
        if(objectSplitPosition == -1) return null;
        objectSplitPosition++;
        if(object.charAt(objectSplitPosition) == '\"') {
            return new Tuple<Integer, JSONFieldType>(object.indexOf("\"", objectSplitPosition + 1) + 1, JSONFieldType.STRING);
        }
        if(object.charAt(objectSplitPosition) == '[') {
            return new Tuple<Integer, JSONFieldType>(object.indexOf("]", objectSplitPosition + 1) + 1, JSONFieldType.ARRAY);
        }
        if(object.charAt(objectSplitPosition) == '{') {
            return new Tuple<Integer, JSONFieldType>(object.indexOf("}", objectSplitPosition + 1) + 1, JSONFieldType.OBJECT);
        }
        if(Character.isDigit(object.charAt(objectSplitPosition))) {
            if(object.substring(objectSplitPosition).contains(".")) {
                return new Tuple<Integer, JSONFieldType>(object.indexOf(",", objectSplitPosition + 1), JSONFieldType.DOUBLE);
            }
            return new Tuple<Integer, JSONFieldType>(object.indexOf(",", objectSplitPosition + 1), JSONFieldType.INTEGER);
        }
        return null;
    }
}
