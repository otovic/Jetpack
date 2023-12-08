package utility.json;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Arrays;

import utility.Tuple;
import utility.json.object.JSONFieldType;
import utility.json.object.JSONObject;
import utility.json.types.JSONChild;
import utility.json.types.JSONRoot;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JSONUtils {
    public static boolean isArray(final String body) {
        return body.indexOf("[") == 0;
    }

    public static StringBuilder removeObjectBrackets(final StringBuilder object) {
        object.deleteCharAt(object.indexOf("{"));
        object.deleteCharAt(object.lastIndexOf("}"));
        return object;
    }

    public static Tuple getFieldLengthAndType(final StringBuilder object, boolean isTest) {
        if (object.toString().equals(""))
            return null;
        int objectSplitPosition = object.indexOf(":");
        if (objectSplitPosition == -1)
            return null;
        objectSplitPosition++;
        if (isTest)
            return new Tuple<Integer, JSONFieldType>(objectSplitPosition, JSONFieldType.OBJECT);
        if (object.charAt(objectSplitPosition) == '\"') {
            return new Tuple<Integer, JSONFieldType>(object.indexOf("\"", objectSplitPosition + 1) + 1,
                    JSONFieldType.STRING);
        }
        if (object.charAt(objectSplitPosition) == '[') {
            return new Tuple<Integer, JSONFieldType>(object.indexOf("]", objectSplitPosition + 1) + 1,
                    JSONFieldType.ARRAY);
        }
        if (object.charAt(objectSplitPosition) == '{') {
            return new Tuple<Integer, JSONFieldType>(getObjectEndPosition(object, objectSplitPosition) + 1,
                    JSONFieldType.OBJECT);
        }
        if (Character.isDigit(object.charAt(objectSplitPosition))) {
            int endPosition = object.indexOf(",", objectSplitPosition) + 1;
            if (endPosition == 0) {
                if (object.substring(objectSplitPosition).contains(".")) {
                    return new Tuple<Integer, JSONFieldType>(object.length(),
                        JSONFieldType.DOUBLE);
                }
                return new Tuple<Integer, JSONFieldType>(object.length(), JSONFieldType.INTEGER);
            }
            if (object.substring(objectSplitPosition, endPosition).contains(".")) {
                return new Tuple<Integer, JSONFieldType>(object.indexOf(",", objectSplitPosition + 1),
                        JSONFieldType.DOUBLE);
            }
            return new Tuple<Integer, JSONFieldType>(object.indexOf(",", objectSplitPosition + 1),
                    JSONFieldType.INTEGER);
        }
        return null;
    }

    public static int getObjectEndPosition(final StringBuilder object, final int startPosition) {
        int rightBrackets = 1;
        int leftBrackets = 0;
        for (int i = startPosition + 1; i < object.length(); i++) {
            if (object.charAt(i) == '{')
                rightBrackets++;
            if (object.charAt(i) == '}')
                leftBrackets++;
            if (rightBrackets == leftBrackets)
                return i;
        }
        if (rightBrackets != leftBrackets)
            throw new RuntimeException("Invalid JSON");
        return -1;
    }

    public static Tuple toKeyAndValue(final String field) {
        String[] fieldParts = field.split(":");
        return new Tuple<String, String>(removeApostrophe(fieldParts[0].trim()),
                removeApostrophe(fieldParts[1].trim()));
    }

    public static String removeApostrophe(final String field) {
        if (field.contains("\""))
            return field.substring(1, field.length() - 1);
        return field;
    }

    public static void removeArrayBrackets(StringBuilder json) {
        json.deleteCharAt(json.indexOf("["));
        json.deleteCharAt(json.lastIndexOf("]"));

        while (json.indexOf(",{") != -1) {
            json.replace(json.indexOf(",{"), json.indexOf(",{") + 1, " ");
        }
    }

    public static JSONObject createJSONObject(final Class<?> type, final String identifier) {
        if (JSONRoot.class.isAssignableFrom(type)) {
            return new JSONObject(type);
        } else if (JSONChild.class.isAssignableFrom(type)) {
            assert identifier != null;
            return new JSONObject(type, identifier);
        } else {
            throw new RuntimeException("Class is not a JSON object");
        }
    }

    public static boolean areFieldTypesCompatible(Class<?> classField, JSONFieldType jsonField) {
        if(classField.getName().equals("int")) {
            if(jsonField == JSONFieldType.INTEGER) return true;
            return false;
        }
        if(classField.getName().equals("double")) {
            if(jsonField == JSONFieldType.DOUBLE) return true;
            return false;
        }
        if (classField == JSONFieldType.getFieldClass(jsonField)) return true;
        return false;
    }

    public static List<String> parseList(final String value) {
        return List.of(value.split(","));
    }

    public static Tuple<?, ?> getHashMapGenericTypes(Field f) {
        Type fieldType = f.getGenericType();
        Type keyType = null;
        Type valueType = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            keyType = typeArguments[0];
            valueType = typeArguments[1];
        }
        return new Tuple(keyType, valueType);
    }

    public static <T> T parseBasedOnClass(final String value, final Class<?> clazz) {
        if (clazz == String.class) {
            return (T) value;
        }
        if (clazz == Integer.class) {
            return (T) Integer.valueOf(value);
        }
        if (clazz == Double.class) {
            return (T) Double.valueOf(value);
        }
        if (clazz == Boolean.class) {
            return (T) Boolean.valueOf(value);
        }
        if (clazz == List.class) {
            return (T) JSONUtils.parseList(value);
        }
        return null;
    }

    public static boolean isDefaultClass(Class<?> desiredClass) {
        List<Class<?>> defaultClasses = Arrays.asList(String.class, Integer.class, Double.class, Boolean.class);
        return defaultClasses.contains(desiredClass);
    }
}
