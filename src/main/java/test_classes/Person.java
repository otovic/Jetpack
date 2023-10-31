package test_classes;

import models.ParamKey;
import models.RoutableFromParams;

@RoutableFromParams
public class Person {
    @ParamKey(field = "name")
    public String name;
    @ParamKey(field = "surname")
    public String surname;
    @ParamKey(field = "age")
    public String age;
    public String address;
}
