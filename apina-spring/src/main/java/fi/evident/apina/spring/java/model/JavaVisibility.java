package fi.evident.apina.spring.java.model;

public enum JavaVisibility {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
