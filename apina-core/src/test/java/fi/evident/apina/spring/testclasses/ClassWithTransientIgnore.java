package fi.evident.apina.spring.testclasses;

import java.beans.Transient;

@SuppressWarnings("unused")
public class ClassWithTransientIgnore {

    @Transient
    public String getFoo() {
        throw new UnsupportedOperationException();
    }

    public String getBar() {
        throw new UnsupportedOperationException();
    }
}
