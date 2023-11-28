package utility.json.object;

public class JSONField {
    final public String name;
    final public String field;
    final public JSONFieldType type;

    public JSONField(final String name, final String value, final JSONFieldType type) {
        this.name = name;
        this.field = value;
        this.type = type;
    }
}
