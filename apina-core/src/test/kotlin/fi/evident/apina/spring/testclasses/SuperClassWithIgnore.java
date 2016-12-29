package fi.evident.apina.spring.testclasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SuperClassWithIgnore {

    @JsonIgnore
    public String getFoo() {
        throw new UnsupportedOperationException();
    }
}
