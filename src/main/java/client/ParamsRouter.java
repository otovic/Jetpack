package client;

import exceptions.RoutingException;
import models.ParamKey;
import models.RoutableFromParams;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public class ParamsRouter {
    public static <T> T routeFromParams(Map<String, String> params, Class<T> object) throws RoutingException {
        try {
            T instance = object.newInstance();
            boolean isRoutable = instance.getClass().isAnnotationPresent(RoutableFromParams.class);
            if(isRoutable) {
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
}
