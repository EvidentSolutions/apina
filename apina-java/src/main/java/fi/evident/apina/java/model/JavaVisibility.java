package fi.evident.apina.java.model;

public enum JavaVisibility {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
