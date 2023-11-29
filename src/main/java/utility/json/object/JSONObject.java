package utility.json.object;

import utility.json.types.JSONChild;
import utility.json.types.JSONRoot;

import java.util.ArrayList;
import java.util.List;

public class JSONObject {
    public String identifier;
    public List<Object> fields;
    
    public JSONObject(final Class<?> type) {
        if(JSONRoot.class.isAssignableFrom(type)) {
            this.identifier = null;
        } else {
            throw new RuntimeException("Class is not a JSON object");
        }

        this.fields = new ArrayList<>();
    }

    public JSONObject(final Class<?> type, final String identifier) {
        if(JSONRoot.class.isAssignableFrom(type)) {
            this.identifier = null;
        }else if(JSONChild.class.isAssignableFrom(type)) {
            assert identifier != null;
            this.identifier = identifier;
        } else {
            throw new RuntimeException("Class is not a JSON object");
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
