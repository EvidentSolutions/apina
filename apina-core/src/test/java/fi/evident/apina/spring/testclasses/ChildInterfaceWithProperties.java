package fi.evident.apina.spring.testclasses;

public interface ChildInterfaceWithProperties extends ParentInterfaceWithProperties {
    default String getBar() {
        return "bar";
    }
}
