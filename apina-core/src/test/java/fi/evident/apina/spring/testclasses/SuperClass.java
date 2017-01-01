package fi.evident.apina.spring.testclasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SuperClass {
    public String foo;
    public String bar;

    @JsonIgnore
    public String baz;
    public String quux;

    @JsonIgnore
    public String getFoo() {
        return foo;
    }
}
