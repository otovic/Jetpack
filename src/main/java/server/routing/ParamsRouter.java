package server.routing;

import exceptions.RoutingException;
import models.ParamKey;
import models.RoutableFromBody;
import models.RoutableFromParams;
import utility.json.JSON;
import utility.json.object.JSONField;
import utility.json.object.JSONObject;
import utility.json.JSONUtils;
import utility.json.types.JSONRoot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ParamsRouter {
    public static <T> T routeFromParams(Map<String, String> params, Class<T> object) throws RoutingException {
        try {
            T instance = object.newInstance();
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
            List<T> instances = new ArrayList<>();
            if (object.newInstance().getClass().isAnnotationPresent(RoutableFromBody.class)) {
                List<StringBuilder> objects = JSON.toListOfObjects(body);
                for (StringBuilder jsonStringObject : objects) {
                    JSONObject jsonObject = JSON.toJSONObject(jsonStringObject, JSONRoot.class, null);
                    T instance = object.newInstance();
                    for (Object field : jsonObject.fields) {
                        Arrays.stream(instance.getClass().getFields())
                                .filter(f -> {
                                    if(field instanceof JSONField) {
                                        JSONField jsonField = (JSONField) field;
                                        return jsonField.name.equals(f.getName());
                                    } else {
                                        return false;
                                    }
                                }).findFirst().ifPresent(f -> {
                                    try {
                                        f.set(instance, ((JSONField) field).field);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                });
                        if (field instanceof JSONObject) {
                            JSONObject objectField = (JSONObject) field;
                            instances.add((T) routeFromBody(objectField.fields.get(0).toString(), object));
                        } else {

                        }
                    }
                }

            } else {
                throw new RoutingException("Class is not routable");
            }
        } catch (Exception e) {
            throw new RoutingException("Could not instantiate object");
        }
    }
}
