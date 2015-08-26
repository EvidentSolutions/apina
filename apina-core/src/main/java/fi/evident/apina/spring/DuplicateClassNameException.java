package fi.evident.apina.spring;

public final class DuplicateClassNameException extends RuntimeException {
    public DuplicateClassNameException(String name1, String name2) {
        super("Translating classes with same simple names is not supported. Conflicting classes: " + name1 + " vs. " + name2);
    }
}
