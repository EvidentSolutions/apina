package fi.evident.apina.spring.testclasses;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class TypeWithOverridingIgnore extends SuperClassWithIgnore {

    @JsonIgnore(false)
    @Override
    public String getFoo() {
        throw new UnsupportedOperationException();
    }
}
