package fi.evident.apina.spring.testclasses;

@SuppressWarnings("unused")
public final class TypeWithOverlappingFieldAndGetter {

    public String foo;

    public String getFoo() {
        return foo;
    }
}
