package fi.evident.apina.spring.testclasses;

@SuppressWarnings("unused")
public class ClassWithTransientFields {

    public String foo;
    public transient String bar;
    private transient String baz;

    public String getBaz() {
        return baz;
    }
}
