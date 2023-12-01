package server.routing;

import exceptions.LoggerException;
import exceptions.RoutingException;
import logger.Log;
import logger.LogType;
import models.ParamKey;
import models.RoutableFromBody;
import models.RoutableFromParams;
import test_classes.Person;
import utility.json.JSON;
import utility.json.object.JSONField;
import utility.json.object.JSONFieldType;
import utility.json.object.JSONObject;
import utility.json.JSONUtils;
import utility.json.types.JSONRoot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamsRouter {
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

    public static <T> List<T> routeFromBody(String body, Class<T> object) throws RoutingException {
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
                                return checkIfCorrespondingFieldExists(f, jsonObject, instance.getClass().getName());
                            })
                            .findFirst()
                            .ifPresent(f -> {
                                try {
                                    if (field instanceof JSONField) {
                                        handleJSONField(f, field, instance);
                                    } else if (field instanceof JSONObject) {
                                        JSONObject jsonObjectO = (JSONObject) field;
                                        if (f.getType() == HashMap.class) {
                                            Type fieldType = f.getGenericType();
                                            Type keyType = null;
                                            Type valueType = null;
                                            if (fieldType instanceof ParameterizedType) {
                                                ParameterizedType parameterizedType = (ParameterizedType) fieldType;
                                                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                                                keyType = typeArguments[0];
                                                valueType = typeArguments[1];
                                            }
                                            HashMap<Object, Object> hashM = new HashMap<Object, Object>();
                                            for (Object hashField : jsonObjectO.fields) {
                                                if (hashField instanceof JSONObject) {
                                                    throw new Exception("Object cannot be routed to primitive type");
                                                } else if (hashField instanceof JSONField) {
                                                    JSONField fld = (JSONField) hashField;
                                                    Class<?> cls = (Class<?>) keyType;
                                                    if (String.class.isAssignableFrom(cls)) {
                                                        hashM.put(fld.name.toString(), fld.field.toString());
                                                    }
                                                    if (Integer.class.isAssignableFrom(cls)) {
                                                        hashM.put(Integer.parseInt(fld.name), fld.field.toString());
                                                    }
                                                    if (Double.class.isAssignableFrom(cls)) {
                                                        hashM.put(Double.parseDouble(fld.name), fld.field.toString());
                                                    }
                                                }
                                            }
                                            f.set(instance, hashM);
                                        }
                                        if (f.getType() == List.class) {
                                            Type cls = f.getGenericType();
                                            if (cls instanceof ParameterizedType) {
                                                ParameterizedType parameterizedType = (ParameterizedType) cls;
                                                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                                                if (typeArguments.length > 0) {
                                                    if (typeArguments[0] == String.class) {
                                                        List<String> list = new ArrayList<String>();
                                                        for (Object hashField : jsonObjectO.fields) {
                                                            if (hashField instanceof JSONObject) {
                                                                throw new Exception(
                                                                        "Object cannot be routed to primitive type");
                                                            } else if (hashField instanceof JSONField) {
                                                                JSONField fld = (JSONField) hashField;
                                                                list.add(fld.field.toString());
                                                            }
                                                        }
                                                        f.set(instance, list);
                                                    }
                                                }
                                            }
                                        }
                                        if (!f.getType().isPrimitive()) {
                                            if (commonClasses.contains(f.getType())) {
                                                throw new Exception("Object cannot be routed to primitive type");
                                            } else {
                                                f.set(instance, routeToClass(jsonObjectO, f.getType()));
                                            }
                                            System.out.println("Object");
                                        }
                                    }
                                } catch (ClassCastException e) {

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

    private static void handleJSONField(Field f, Object field, Object instance) throws LoggerException {
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

    private static boolean checkIfCorrespondingFieldExists(Field f, Object jsonObject, String instanceName)
            throws LoggerException {
        if (jsonObject instanceof JSONField) {
            if (f.getName().equals(((JSONField) jsonObject).name)) {
                if (f.getClass().isAnnotationPresent(ParamKey.class)) {
                    return f.getAnnotation(ParamKey.class).field()
                            .equals(((JSONField) jsonObject).name);
                } else {
                    Log.log(LogType.ERROR, true, "Found a field: " + f.getName()
                            + " in a class: " + instanceName
                            + " that is not annotated with @ParamKey, therefore it cannot be routed");
                    return false;
                }
            }
            return false;
        }
        if (jsonObject instanceof JSONObject) {
            if (f.getName().equals(((JSONObject) jsonObject).identifier)) {
                if (f.getClass().isAnnotationPresent(ParamKey.class)) {
                    return f.getAnnotation(ParamKey.class).field()
                            .equals(((JSONObject) jsonObject).identifier);
                } else {
                    Log.log(LogType.ERROR, true, "Found a field: " + f.getName()
                            + " in a class: " + instanceName
                            + " that is not annotated with @ParamKey, therefore it cannot be routed");
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    private static void setClassFieldFromJSONField(Field classField, JSONField jsonField, Object instance) {
        if (JSONUtils.areFieldTypesCompatible(f.getType(), jsonField.type)) {
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

    private static boolean isDefaultClass(Class<?> desiredClass) {
        Set<Class<?>> commonClasses = new HashSet<Class<?>>();
        commonClasses.add(String.class);
        commonClasses.add(Integer.class);
        commonClasses.add(Double.class);
        commonClasses.add(List.class);
        commonClasses.add(HashMap.class);
    }

    public static <T> T routeToClass(JSONObject object, Class<T> desiredClass) throws Exception {
        try {
            if (!desiredClass.getClass().isAnnotationPresent(RoutableFromBody.class)) {
                throw new Exception("Class is not routable");
            }
            T instance = desiredClass.getDeclaredConstructor().newInstance();
            return instance;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException e) {
            return null;
        }
    }
}