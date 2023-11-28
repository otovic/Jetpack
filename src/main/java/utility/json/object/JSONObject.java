package utility.json.object;

import utility.json.types.JSONChild;
import utility.json.types.JSONRoot;

import java.util.ArrayList;
import java.util.List;

public class JSONObject {
    List<Object> fields;
    public JSONObject(Class<?> type) {
        if(type.getClass().equals(JSONRoot.class)) {
            System.out.println("CLASS IS ROOT");
        }
        if(type.getClass().equals(JSONChild.class)) {
            System.out.println("CLASS IS ROOT");
        }
        this.fields = new ArrayList<>();
    }

    public void addField(final JSONField field) {
        fields.add(field);
    }

    public void addObject(final JSONObject object) {
        fields.add(object);
    }
}
