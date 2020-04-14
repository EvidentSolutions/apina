package fi.evident.apina.spring.testclasses;

import org.springframework.data.annotation.Transient;

@SuppressWarnings("unused")
public class ClassWithSpringDataTransient {

    @Transient
    private String baz;

    public String getFoo() {
        throw new UnsupportedOperationException();
    }

    @Transient
    public String getBar() {
        throw new UnsupportedOperationException();
    }

    public String getBaz() {
        return baz;
    }
}
