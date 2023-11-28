package utility.json;

import utility.Tuple;
import utility.json.object.JSONField;
import utility.json.object.JSONFieldType;
import utility.json.object.JSONObject;
import utility.json.types.JSONChild;
import utility.json.types.JSONRoot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JSON {
    public static <T> List<StringBuilder> toListOfObjects(final String json) {
        StringBuilder jsonBuilder = new StringBuilder(json);
        List<StringBuilder> objects = new ArrayList<>();
        if(JSONUtils.isArray(json)) {
            removeArrayBrackets(jsonBuilder);
            objects = getObjectsInJsonArray(jsonBuilder);
        }
        return objects;
    }

//    public static <T> T toListOfJSONObjects(final List<StringBuilder> objects, final Class<T> object) throws InstantiationException, IllegalAccessException {
//        List<JSONField> jsonFields = JSONObjectToListOfFields(removeObjectBrackets(objects));
//
//        T instance;
//        if(object.isAnnotationPresent(RoutableFromBody.class)) {
//            instance = object.newInstance();
//            Arrays.stream(object.getDeclaredFields())
//                    .filter(field -> field.isAnnotationPresent(ParamKey.class))
//                    .forEach(field -> {
//                        ParamKey paramKey = field.getAnnotation(ParamKey.class);
//                        String key = paramKey.field();
//                        jsonFields.stream()
//                                .filter(jsonField -> jsonField.name.equals(key))
//                                .findFirst()
//                                .ifPresent(jsonField -> {
//                                    try {
//                                        field.set(instance, parseJSONFieldTypeBasedOnClassFieldType(field, jsonField.field));
//                                    } catch (Exception e) {
//                                        System.out.println("PETAR");
//                                        e.printStackTrace();
//                                    }
//                                });
//                    });
//            return instance;
//        } else {
//            instance = null;
//            System.out.println("Class is not routable");
//        }
//        return null;
//    }

    protected static <T> Object parseJSONFieldTypeBasedOnClassFieldType(final Field field, final String value) {
        if(field.getType().equals(String.class)) return value;
        if(field.getType().equals(Integer.class)) return Integer.parseInt(value);
        if(field.getType().equals(Double.class)) return Double.parseDouble(value);
        if(field.getType().equals(List.class)) return Arrays.asList(value.split(","));
        return value;
    }

    public static <T> JSONObject toJSONObject(final StringBuilder object, final Class<T> type) {
        try {
            JSONUtils.removeObjectBrackets(object);
            JSONObject jsonObject = new JSONObject(type);
            while (JSONUtils.getFieldLengthAndType(object) != null) {
                Tuple<String, String> parsedField = null;
                Tuple<Integer, JSONFieldType> fieldLengthAndType = JSONUtils.getFieldLengthAndType(object);
                String field = object.substring(0, fieldLengthAndType.first);
                if (fieldLengthAndType.second == JSONFieldType.OBJECT) {
                    String[] fieldParts = field.split(":", 2);
                    parsedField = new Tuple<String, String>(removeApostrophe(fieldParts[0].trim()), fieldParts[1].trim().substring(1, fieldParts[1].trim().length() - 1));
//                    JSONObject lala = toJSONObject(new StringBuilder(parsedField.second), JSONChild.class);
//                    jsonObject.addObject(toJSONObject(new StringBuilder(parsedField.second), JSONChild.class));
                } else {
                    parsedField = toKeyAndValue(field);
                    jsonObject.addField(new JSONField(parsedField.first, parsedField.second, fieldLengthAndType.second));
                }
                object.delete(0, fieldLengthAndType.first + 1);
            }
            return jsonObject;
        } catch (Exception e) {
            System.out.println("Erorr: " + e.getMessage());
            return null;
        }
    }

    private static Tuple toKeyAndValue(final String field) {
        String[] fieldParts = field.split(":");
        return new Tuple<String, String>(removeApostrophe(fieldParts[0].trim()), removeApostrophe(fieldParts[1].trim()));
    }

    private static String removeApostrophe(final String field) {
        if(field.contains("\"")) return field.substring(1, field.length() - 1);
        return field;
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
