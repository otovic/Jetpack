package utility.json.object;

import java.util.HashMap;
import java.util.List;

public enum JSONFieldType {
    STRING,
    INTEGER,
    DOUBLE,
    OBJECT,
    ARRAY;

    public static Class<?> getFieldType(final JSONFieldType field) {
        if(field == INTEGER) return Integer.class;
        if(field == DOUBLE) return Double.class;
        if(field == STRING) return String.class;
        if(field == OBJECT) return HashMap.class;
        if(field == ARRAY) return List.class;
        return null;
    }
}
