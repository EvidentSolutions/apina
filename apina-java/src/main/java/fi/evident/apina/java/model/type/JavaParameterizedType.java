package fi.evident.apina.java.model.type;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Represents a generic Java type, like {@link java.lang.reflect.ParameterizedType}.
 */
public final class JavaParameterizedType extends JavaType {

    public final JavaType baseType;

    public final List<JavaType> arguments;

    public JavaParameterizedType(JavaType baseType, List<JavaType> arguments) {
        if (arguments.isEmpty()) throw new IllegalArgumentException("no arguments for generic type");

        this.baseType = requireNonNull(baseType);
        this.arguments = requireNonNull(arguments);
    }

    public List<JavaType> getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaParameterizedType that = (JavaParameterizedType) o;

        return baseType.equals(that.baseType) && arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseType, arguments);
    }

    @Override
    public String toString() {
        return baseType + arguments.stream().map(JavaType::toString).collect(joining(", ", "<", ">"));
    }
}
