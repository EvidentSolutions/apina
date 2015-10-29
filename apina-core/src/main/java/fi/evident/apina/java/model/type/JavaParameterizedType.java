package fi.evident.apina.java.model.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fi.evident.apina.utils.CollectionUtils.join;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Represents a generic Java type, like {@link java.lang.reflect.ParameterizedType}.
 */
public final class JavaParameterizedType extends JavaType {

    public final JavaType baseType;

    public final List<JavaType> arguments;

    public JavaParameterizedType(JavaType baseType, List<? extends JavaType> arguments) {
        if (arguments.isEmpty()) throw new IllegalArgumentException("no arguments for generic type");

        this.baseType = requireNonNull(baseType);
        this.arguments = unmodifiableList(new ArrayList<>(arguments));
    }

    @Override
    public String getNonGenericClassName() {
        return baseType.getNonGenericClassName();
    }

    public JavaType getBaseType() {
        return baseType;
    }

    public List<JavaType> getArguments() {
        return arguments;
    }

    @Override
    public <C, R> R accept(JavaTypeVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
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
        return baseType + join(arguments, ", ", "<", ">");
    }
}
