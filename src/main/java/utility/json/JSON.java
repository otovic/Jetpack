package utility.json;

import models.ParamKey;
import models.RoutableFromBody;
import test_classes.Person;
import utility.Tuple;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JSON {
    public static <T> List<StringBuilder> toListOfJSONObjects(final String json) {
        StringBuilder jsonBuilder = new StringBuilder(json);
        List<StringBuilder> objects = new ArrayList<>();
        if(isArray(jsonBuilder) != -1) {
            removeArrayBrackets(jsonBuilder);
            objects = getObjectsInJsonArray(jsonBuilder);
        }
        return objects;
    }

    public static <T> T toObject(final StringBuilder jsonObject, final Class<T> object) throws InstantiationException, IllegalAccessException {
        removeObjectBrackets(jsonObject);
        List<JSONField> jsonFields = JSONObjectToListOfFields(jsonObject);
        T instance;
        if(object.isAnnotationPresent(RoutableFromBody.class)) {
            instance = object.newInstance();
            Arrays.stream(object.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(ParamKey.class))
                    .forEach(field -> {
                        ParamKey paramKey = field.getAnnotation(ParamKey.class);
                        String key = paramKey.field();
                        jsonFields.stream()
                                .filter(jsonField -> jsonField.name.equals(key))
                                .findFirst()
                                .ifPresent(jsonField -> {
                                    try {
                                        System.out.println(field.getType());
                                        System.out.println("Petar");
                                        field.set(instance, jsonField.field);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                    });
            System.out.println(instance);
        } else {
            instance = null;
            System.out.println("Class is not routable");
        }
        return null;
    }

    private static List<JSONField> JSONObjectToListOfFields(final StringBuilder object) {
        List<JSONField> jsonFields = new ArrayList<>();
        while (getFieldLengthAndType(object) != null) {
            Tuple<String, String> parsedField = null;
            Tuple<Integer, JSONFieldType> fieldLengthAndType = getFieldLengthAndType(object);
            String field = object.substring(0, fieldLengthAndType.first);
            if(fieldLengthAndType.second == JSONFieldType.OBJECT) {
                String[] fieldParts = field.split(":", 2);
                parsedField = new Tuple<String, String>(removeApostrophe(fieldParts[0].trim()), fieldParts[1].trim().substring(1, fieldParts[1].trim().length() - 1));
                jsonFields.add(new JSONField(parsedField.first, parsedField.second, fieldLengthAndType.second));
            } else {
                parsedField = toKeyAndValue(field);
                jsonFields.add(new JSONField(parsedField.first, parsedField.second, fieldLengthAndType.second));
            }
            object.delete(0, fieldLengthAndType.first + 1);
        }
        return jsonFields;
    }

    private static Tuple getFieldLengthAndType(final StringBuilder object) {
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
            return new Tuple<Integer, JSONFieldType>(object.indexOf(",", objectSplitPosition + 1) + 1, JSONFieldType.INTEGER);
        }
        return null;
    }

    private static Tuple toKeyAndValue(final String field) {
        String[] fieldParts = field.split(":");
        return new Tuple<String, String>(removeApostrophe(fieldParts[0].trim()), removeApostrophe(fieldParts[1].trim()));
    }

    private static String removeApostrophe(final String field) {
        if(field.contains("\"")) return field.substring(1, field.length() - 1);
        return field;
    }

    private static int isArray(StringBuilder json) {
        return json.indexOf("[");
    }

    private static StringBuilder removeObjectBrackets(final StringBuilder object) {
        object.deleteCharAt(object.indexOf("{"));
        object.deleteCharAt(object.lastIndexOf("}"));
        return object;
    }

    private static void removeArrayBrackets(StringBuilder json) {
        json.deleteCharAt(json.indexOf("["));
        json.deleteCharAt(json.lastIndexOf("]"));

        while (json.indexOf(",{") != -1) {
            json.replace(json.indexOf(",{"), json.indexOf(",{") + 1, " " );
        }
    }

    private static List<StringBuilder> getObjectsInJsonArray(StringBuilder json) {
        List<StringBuilder> objects = new ArrayList<>();
        do {
            boolean lastObject = false;
            StringBuilder object = null;
            int startPosition = json.indexOf("{");
            int endPosition = json.indexOf("} {");
            if(startPosition == -1) return objects;
            if(endPosition == -1) lastObject = true;

            if(lastObject) {
                object = new StringBuilder(json.toString());
                objects.add(object);
                return objects;
            } else {
                object = new StringBuilder(json.substring(startPosition, endPosition + 1));
                objects.add(object);
                json.delete(startPosition, endPosition + 2);
            }
        } while (json.indexOf("{") != -1);

        return objects;
    }
}
