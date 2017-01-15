package fi.evident.apina.spring.testclasses;

@SuppressWarnings({"unused", "CanBeFinal"})
public final class TypeWithOverlappingFieldAndGetter {

    public String foo;

    public String getFoo() {
        return foo;
    }
}
