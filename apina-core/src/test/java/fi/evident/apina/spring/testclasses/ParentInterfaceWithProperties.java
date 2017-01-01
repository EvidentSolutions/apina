package fi.evident.apina.spring.testclasses;

public interface ParentInterfaceWithProperties {
    default String getFoo() {
        return "foo";
    }
}
