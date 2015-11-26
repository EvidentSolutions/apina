package fi.evident.apina.java.model.type;

import fi.evident.apina.java.model.JavaClass;

import static java.util.Objects.requireNonNull;

/**
 * Represents a class bound to a type-environment.
 */
public final class BoundClass {

    private final JavaClass javaClass;
    private final TypeEnvironment environment;

    public BoundClass(JavaClass javaClass, TypeEnvironment environment) {
        this.javaClass = requireNonNull(javaClass);
        this.environment = requireNonNull(environment);
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public TypeEnvironment getEnvironment() {
        return environment;
    }
}
