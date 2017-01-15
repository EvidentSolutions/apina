package fi.evident.apina.spring.testclasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("WeakerAccess")
public class SuperClassWithIgnore {

    @SuppressWarnings("unused")
    @JsonIgnore
    public String getFoo() {
        throw new UnsupportedOperationException();
    }
}
