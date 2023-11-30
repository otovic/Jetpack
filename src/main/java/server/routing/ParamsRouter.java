package server.routing;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            if (object.getDeclaredConstructor().newInstance().getClass().isAnnotationPresent(RoutableFromBody.class)) {
                List<StringBuilder> objects = JSON.toListOfObjects(body);
                System.out.println("here");
                for (StringBuilder jsonStringObject : objects) {
                    JSONObject jsonObject = JSON.toJSONObject(jsonStringObject, JSONRoot.class, null);
                    T instance = object.getDeclaredConstructor().newInstance();
                    for (Object field : jsonObject.fields) {
                        Arrays.stream(instance.getClass().getFields())
                                .filter(f -> {
                                    if(field instanceof JSONField) return f.getName().equals(((JSONField) field).name);
                                    if(field instanceof JSONObject) return f.getName().equals(((JSONObject) field).identifier);
                                    return false;
                                })
                                .findFirst()
                                .ifPresent(f -> {
                                    try {
                                        if(field instanceof JSONField) {
                                            JSONField jsonField = (JSONField) field;
                                            if (JSONUtils.areFieldTypesCompatible(f.getType(), jsonField.type)) {
                                                if(JSONFieldType.getFieldClass(jsonField.type) == Integer.class) {
                                                    f.set(instance, Integer.parseInt(jsonField.field.toString()));
                                                    return;
                                                }
                                                if(JSONFieldType.getFieldClass(jsonField.type) == Double.class) {
                                                    f.set(instance, Double.parseDouble(jsonField.field.toString()));
                                                    return;
                                                }
                                                if(JSONFieldType.getFieldClass(jsonField.type) == List.class) {
                                                    f.set(instance, JSONUtils.parseListFieldType(jsonField.field));
                                                    return;
                                                }
                                                f.set(instance, jsonField.field);
                                            } else {
                                                Log.log(LogType.ERROR, true, "Field " + f.getName() + ", in class " + instance.getClass().getName() + ", of type " + f.getType().getName() + " is not compatible with JSONField type " + JSONFieldType.getFieldClass(jsonField.type));
                                            }
                                        } else if(field instanceof JSONObject) {
                                            JSONObject jsonObjectO = (JSONObject) field;
                                            if(f.getType() == HashMap.class) {
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
                                                        if(String.class.isAssignableFrom(cls)) {
                                                            hashM.put(fld.name.toString(), fld.field.toString());
                                                        }
                                                        if (Integer.class.isAssignableFrom(cls)) {
                                                            System.out.println(Integer.parseInt(fld.name));
                                                            hashM.put(Integer.parseInt(fld.name), fld.field.toString());
                                                        }
                                                        if (Double.class.isAssignableFrom(cls)) {
                                                            hashM.put(Double.parseDouble(fld.name), fld.field.toString());
                                                        }
                                                    }
                                                }
                                                f.set(instance, hashM);
                                            }
                                        }
                                    } catch (ClassCastException e) {
                                        
                                    } 
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                    instances.add(instance);
                }
            } else {
                throw new RoutingException("Class is not routable");
            }
            return instances;
        } catch (Exception e) {
            throw new RoutingException("Could not instantiate object");
        }
    }
}
