package test_classes;

import models.ParamKey;
import models.RoutableFromBody;

@RoutableFromBody
public class Address {
    @ParamKey(field = "zip")
    public String zip;
    @ParamKey(field = "street")
    public String street;
}
