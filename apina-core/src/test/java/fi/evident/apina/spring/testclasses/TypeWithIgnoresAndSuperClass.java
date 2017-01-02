package fi.evident.apina.spring.testclasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("unused")
public final class TypeWithIgnoresAndSuperClass extends SuperClass {
    public String baz;

    @JsonIgnore
    public String quux;

    @Override
    public String getFoo() {
        return foo;
    }
}
