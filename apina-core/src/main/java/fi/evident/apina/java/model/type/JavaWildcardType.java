package fi.evident.apina.java.model.type;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Represents a wildcard type, like {@link java.lang.reflect.WildcardType}.
 */
public final class JavaWildcardType extends JavaType {

    private final Optional<JavaType> upperBound;
    private final Optional<JavaType> lowerBound;

    public JavaWildcardType(Optional<JavaType> upperBound, Optional<JavaType> lowerBound) {
        this.upperBound = requireNonNull(upperBound);
        this.lowerBound = requireNonNull(lowerBound);
    }

    public static JavaType unbounded() {
        return new JavaWildcardType(Optional.empty(), Optional.empty());
    }

    public static JavaType extending(JavaType javaType) {
        return new JavaWildcardType(Optional.of(javaType), Optional.empty());
    }

    public static JavaType withSuper(JavaType javaType) {
        return new JavaWildcardType(Optional.empty(), Optional.of(javaType));
    }

    public Optional<JavaType> getUpperBound() {
        return upperBound;
    }

    public Optional<JavaType> getLowerBound() {
        return lowerBound;
    }

    @Override
    public String getNonGenericClassName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C, R> R accept(JavaTypeVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public JavaType resolve(TypeEnvironment env) {
        return new JavaWildcardType(
                upperBound.map(t -> t.resolve(env)),
                lowerBound.map(t1 -> t1.resolve(env)));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("?");

        if (upperBound.isPresent())
            sb.append(" extends ").append(upperBound.get());

        if (lowerBound.isPresent())
            sb.append(" super ").append(lowerBound.get());

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaWildcardType that = (JavaWildcardType) o;

        return upperBound.equals(that.upperBound) && lowerBound.equals(that.lowerBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upperBound, lowerBound);
    }
}
