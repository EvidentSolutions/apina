package fi.evident.apina.spring.testclasses;

import com.fasterxml.jackson.annotation.JsonValue;

public final class ClassWithJsonValue {
    public String bar;
    public String baz;

    @JsonValue
    public String foo() {
        return "foo";
    }
}
