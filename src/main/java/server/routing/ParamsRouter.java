package server.routing;

import exceptions.RoutingException;
import models.ParamKey;
import models.RoutableFromBody;
import models.RoutableFromParams;
import utility.json.JSON;
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
            if(instance.getClass().isAnnotationPresent(RoutableFromParams.class)) {
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

    public static <T> T routeFromBody(String body, Class<T> object) throws RoutingException {
        try {
            T instance = object.newInstance();
            if(instance.getClass().isAnnotationPresent(RoutableFromBody.class)) {
                if(JSONUtils.isArray(body)) {
                    List<StringBuilder> objects = JSON.toListOfObjects(body);
                    List<JSONObject> instanceObjects = new ArrayList<>();
                    for (StringBuilder jsonStringObject : objects) {
                        instanceObjects.add(JSON.toJSONObject(jsonStringObject, JSONRoot.class, null));
                    }
                    System.out.println("BREAKPOINT");
                }
            } else {
                throw new RoutingException("Class is not routable");
            }
            return instance;
        } catch (Exception e) {
            throw new RoutingException("Could not instantiate object");
        }
    }
}
