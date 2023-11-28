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
            JSONUtils.removeArrayBrackets(jsonBuilder);
            objects = getObjectsInJsonArray(jsonBuilder);
        }
        return objects;
    }

    protected static <T> Object parseJSONFieldTypeBasedOnClassFieldType(final Field field, final String value) {
        if(field.getType().equals(String.class)) return value;
        if(field.getType().equals(Integer.class)) return Integer.parseInt(value);
        if(field.getType().equals(Double.class)) return Double.parseDouble(value);
        if(field.getType().equals(List.class)) return Arrays.asList(value.split(","));
        return value;
    }

    public static JSONObject toJSONObject(final StringBuilder object, final Class<?> type, String identifier) {
        try {
            JSONObject jsonObject;
            if(JSONChild.class.isAssignableFrom(type)) {
                if (identifier == null) identifier = "";
                jsonObject = new JSONObject(type, identifier);
            } else if(JSONRoot.class.isAssignableFrom(type)) {
                JSONUtils.removeObjectBrackets(object);
                jsonObject = new JSONObject(type);
            } else {
                throw new RuntimeException("Class is not a JSON object");
            }
            while (JSONUtils.getFieldLengthAndType(object, true) != null) {
                Tuple<String, String> parsedField = null;
                Tuple<Integer, JSONFieldType> fieldLengthAndType = JSONUtils.getFieldLengthAndType(object, false);
                assert fieldLengthAndType != null;
                String field = object.substring(0, fieldLengthAndType.first);
                if (fieldLengthAndType.second == JSONFieldType.OBJECT) {
                    String[] fieldParts = field.split(":", 2);
                    parsedField = new Tuple<String, String>(JSONUtils.removeApostrophe(fieldParts[0].trim()), fieldParts[1].trim().substring(1, fieldParts[1].trim().length() - 1));
                    jsonObject.addObject(toJSONObject(new StringBuilder(parsedField.second), JSONChild.class, parsedField.first));
                } else {
                    parsedField = JSONUtils.toKeyAndValue(field);
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
