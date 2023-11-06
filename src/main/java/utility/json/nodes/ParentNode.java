package utility.json.nodes;

import java.util.List;

public class ParentNode extends Node<String>{
    public final String value;
    public List<ChildNode> children;
    public ParentNode(String value) {
        this.value = value;
    }
}
