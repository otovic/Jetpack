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
    public double age;
    @ParamKey(field = "interests")
    public List<String> interests;
    @ParamKey(field = "address")
    public Address address;
}
