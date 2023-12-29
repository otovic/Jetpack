package test_classes;

import models.ParamKey;
import models.RoutableFromBody;
import models.RoutableFromParams;

import java.util.HashMap;
import java.util.List;

@RoutableFromParams
@RoutableFromBody
public class Person {
    @ParamKey(field = "name")
    public String name;
    @ParamKey(field = "age")
    public String age;

    public Person(String name, String age) {
        this.name = name;
        this.age = age;
    }
}
