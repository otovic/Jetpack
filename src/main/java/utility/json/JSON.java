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

import exceptions.LoggerException;
import exceptions.RoutingException;
import logger.Log;
import logger.LogType;
import models.ParamKey;
import models.RoutableFromBody;
import models.RoutableFromParams;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JSON {
    public static <T> T routeFromParams(Map<String, String> params, Class<T> object) throws RoutingException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        try {
            T instance = object.getDeclaredConstructor().newInstance();
            if (instance.getClass().isAnnotationPresent(RoutableFromParams.class)) {
                Arrays.stream(instance.getClass().getFields())
                        .filter(field -> field.isAnnotationPresent(ParamKey.class))
                        .forEach(field -> {
                            ParamKey paramKey = field.getAnnotation(ParamKey.class);
                            String key = paramKey.field();
                            String value = params.get(key);
                            try {
                                field.set(instance, value);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
            } else {
                throw new RoutingException("Class is not routable");
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RoutingException("Could not instantiate object");
        }
    }

    public static <T> List<T> routeFromBody(String body, Class<T> object) throws RoutingException, LoggerException {
        try {
            List<T> instances = new ArrayList<T>();

            if (object.getDeclaredConstructor().getClass().isAnnotationPresent(RoutableFromBody.class)) {
                throw new RoutingException("Class is not routable");
            }

            List<StringBuilder> objects = JSON.toListOfObjects(body);
            for (StringBuilder jsonStringObject : objects) {
                JSONObject jsonObject = JSON.toJSONObject(jsonStringObject, JSONRoot.class, null);
                T instance = object.getDeclaredConstructor().newInstance();
                for (Object field : jsonObject.fields) {
                    Arrays.stream(instance.getClass().getFields())
                            .filter(f -> {
                                try {
                                    return checkIfCorrespondingFieldExists(f, field, instance.getClass().getName());
                                } catch (LoggerException e) {
                                    return false;
                                }
                            })
                            .findFirst()
                            .ifPresent(f -> {
                                try {
                                    if (field instanceof JSONField) {
                                        handleJSONField(f, field, instance);
                                    } else {
                                        handleJSONObject(f, field, instance);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                }
                instances.add(instance);
            }
            return instances;
        } catch (Exception e) {
            throw new RoutingException("Could not instantiate object");
        }
    }

    protected static void handleJSONField(Field f, Object field, Object instance)
            throws LoggerException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
        JSONField jsonField = (JSONField) field;
        if (JSONUtils.areFieldTypesCompatible(f.getType(), jsonField.type)) {
            setClassFieldFromJSONField(f, jsonField, instance);
        } else {
            Log.log(LogType.ERROR, true, "Field " + f.getName() + ", in class "
                    + instance.getClass().getName() + ", of type "
                    + f.getType().getName() + " is not compatible with JSONField type "
                    + JSONFieldType.getFieldClass(jsonField.type));
        }
    }

    protected static void handleJSONObject(Field f, Object field, Object instance) throws Exception {
        JSONObject jsonObject = (JSONObject) field;
        if (f.getType() == HashMap.class) {
            f.set(instance, handleHashMap(f, jsonObject));
            return;
        }
        if (f.getType() == List.class) {
            f.set(instance, handleList(f, jsonObject, instance));
            return;
        }
        if (JSONUtils.isDefaultClass(f.getType())) {
            throw new Exception("Object cannot be routed to primitive type");
        } else {
            f.set(instance, routeToClass(jsonObject, f.getType()));
        }
    }

    protected static List handleList(Field f, JSONObject jsonObject, Object instance) throws Exception {
        Type genericType = f.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            Log.log(LogType.ERROR, false, "Error while trying to get generic type of field: " + f.getName()
                    + " in class: " + instance.getClass().getName());
            throw new Exception("Error while trying to get generic type of field: " + f.getName()
                    + " in class: " + instance.getClass().getName());
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] typeArguments = parameterizedType.getActualTypeArguments();

        if (typeArguments.length == 0) {
            Log.log(LogType.ERROR, false, "Error while trying to get generic type of field: " + f.getName()
                    + " in class: " + instance.getClass().getName());
            throw new Exception("Error while trying to get generic type of field: " + f.getName()
                    + " in class: " + instance.getClass().getName());
        }

        List<?> list = new ArrayList();
        for (Object field : jsonObject.fields) {
            if (field instanceof JSONObject) {
                throw new Exception(
                        "Object cannot be routed to primitive type");
            } else {
                JSONField jsonField = (JSONField) field;
                list.add(JSONUtils.parseBasedOnClass(jsonField.field, (Class<?>) typeArguments[0]));
            }
        }
        return list;
    }

    protected static HashMap handleHashMap(Field f, JSONObject object) throws Exception {
        Tuple genericTypes = JSONUtils.getHashMapGenericTypes(f);
        HashMap<?, ?> hashM = new HashMap();
        for (Object hashField : object.fields) {
            if (hashField instanceof JSONObject) {
                throw new Exception("Object cannot be routed to primitive type");
            } else {
                JSONField jsonField = (JSONField) hashField;
                Class<?> keyClass = (Class<?>) genericTypes.first;
                Class<?> valueClass = (Class<?>) genericTypes.second;
                hashM.put(JSONUtils.parseBasedOnClass(jsonField.name, keyClass),
                        JSONUtils.parseBasedOnClass(jsonField.field, valueClass));
            }
        }
        return hashM;
    }

    protected static boolean checkIfCorrespondingFieldExists(Field f, Object jsonObject, String instanceName)
            throws LoggerException {
        if (jsonObject instanceof JSONField) {
            if (!f.getName().equals(((JSONField) jsonObject).name))
                return false;
            if (f.isAnnotationPresent(ParamKey.class)) {
                return f.getAnnotation(ParamKey.class).field()
                        .equals(((JSONField) jsonObject).name);
            }
            Log.log(LogType.ERROR, true, "Found a field: " + f.getName()
                    + " in a class: " + instanceName
                    + " with a corresponding name that is not annotated with @ParamKey, therefore it cannot be routed");
            return false;
        }
        if (jsonObject instanceof JSONObject) {
            if (!f.getName().equals(((JSONObject) jsonObject).identifier))
                return false;
            if (f.isAnnotationPresent(ParamKey.class)) {
                return f.getAnnotation(ParamKey.class).field()
                        .equals(((JSONObject) jsonObject).identifier);
            }
            Log.log(LogType.ERROR, true, "Found a field: " + f.getName()
                    + " in a class: " + instanceName
                    + " with a corresponding name that is not annotated with @ParamKey, therefore it cannot be routed");
            return false;
        }
        return false;
    }

    protected static void setClassFieldFromJSONField(Field classField, JSONField jsonField, Object instance)
            throws NumberFormatException, IllegalArgumentException, IllegalAccessException, LoggerException {
        if (JSONUtils.areFieldTypesCompatible(classField.getType(), jsonField.type)) {
            if (JSONFieldType.getFieldClass(jsonField.type) == Integer.class) {
                classField.set(instance, Integer.parseInt(jsonField.field.toString()));
                return;
            }
            if (JSONFieldType.getFieldClass(jsonField.type) == Double.class) {
                classField.set(instance, Double.parseDouble(jsonField.field.toString()));
                return;
            }
            if (JSONFieldType.getFieldClass(jsonField.type) == List.class) {
                classField.set(instance, JSONUtils.parseList(jsonField.field));
                return;
            }
            classField.set(instance, jsonField.field);
        } else {
            Log.log(LogType.ERROR, true,
                    "Field " + classField.getName() + ", in class " + instance.getClass().getName() + ", of type "
                            + classField.getType().getName() + " is not compatible with JSONField type "
                            + JSONFieldType.getFieldClass(jsonField.type));
        }
    }

    public static <T> T routeToClass(JSONObject object, Class<T> desiredClass) throws Exception {
        try {
            if (desiredClass.getClass().isAnnotationPresent(RoutableFromBody.class)) {
                throw new RoutingException("Class is not routable");
            }

            T instance = desiredClass.getDeclaredConstructor().newInstance();
            for (Object field : object.fields) {
                Arrays.stream(instance.getClass().getFields())
                        .filter(f -> {
                            try {
                                return checkIfCorrespondingFieldExists(f, field, instance.getClass().getName());
                            } catch (LoggerException e) {
                                return false;
                            }
                        })
                        .findFirst()
                        .ifPresent(f -> {
                            try {
                                if (field instanceof JSONField) {
                                    handleJSONField(f, field, instance);
                                } else {
                                    handleJSONObject(f, field, instance);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
            }
            return instance;
        } catch (Exception e) {
            throw new RoutingException("Could not instantiate object");
        }
    }

    public static <T> List<StringBuilder> toListOfObjects(final String json) {
        StringBuilder jsonBuilder = new StringBuilder(json);
        List<StringBuilder> objects = new ArrayList<>();
        if (JSONUtils.isArray(json)) {
            JSONUtils.removeArrayBrackets(jsonBuilder);
            objects = getObjectsInJsonArray(jsonBuilder);
        } else {
            objects = getObjectsInJsonArray(jsonBuilder);
        }
        return objects;
    }

    protected static <T> Object parseJSONFieldTypeBasedOnClassFieldType(final Field field, final String value) {
        if (field.getType().equals(String.class))
            return value;
        if (field.getType().equals(Integer.class))
            return Integer.parseInt(value);
        if (field.getType().equals(Double.class))
            return Double.parseDouble(value);
        if (field.getType().equals(List.class))
            return Arrays.asList(value.split(","));
        return value;
    }

    public static JSONObject toJSONObject(final StringBuilder object, final Class<?> type, String identifier) {
        try {
            JSONObject jsonObject;
            if (JSONChild.class.isAssignableFrom(type)) {
                if (identifier == null)
                    identifier = "";
                jsonObject = new JSONObject(type, identifier);
            } else if (JSONRoot.class.isAssignableFrom(type)) {
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
                    parsedField = new Tuple<String, String>(JSONUtils.removeApostrophe(fieldParts[0].trim()),
                            fieldParts[1].trim().substring(1, fieldParts[1].trim().length() - 1));
                    jsonObject.addObject(
                            toJSONObject(new StringBuilder(parsedField.second), JSONChild.class, parsedField.first));
                } else {
                    parsedField = JSONUtils.toKeyAndValue(field);
                    jsonObject
                            .addField(new JSONField(parsedField.first, parsedField.second, fieldLengthAndType.second));
                }
                object.delete(0, fieldLengthAndType.first + 1);
            }
            return jsonObject;
        } catch (Exception e) {
            System.out.println("Erorr: " + e.getMessage());
            return null;
        }
    }

    protected static List<StringBuilder> getObjectsInJsonArray(StringBuilder json) {
        List<StringBuilder> objects = new ArrayList<>();
        do {
            boolean lastObject = false;
            StringBuilder object = null;
            int startPosition = json.indexOf("{");
            int endPosition = json.indexOf("} {");
            if (startPosition == -1)
                return objects;
            if (endPosition == -1)
                lastObject = true;

            if (lastObject) {
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
