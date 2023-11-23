package utility.json;

import java.util.List;

public class JSONObject {
    List<JSONField> fields;
    public JSONObject(final List<JSONField> fields) {
        this.fields = fields;
    }
}
