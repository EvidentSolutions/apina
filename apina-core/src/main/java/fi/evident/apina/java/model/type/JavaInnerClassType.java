package fi.evident.apina.java.model.type;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class JavaInnerClassType extends JavaType {

    private final JavaType outer;
    private final String name;

    public JavaInnerClassType(JavaType outer, String name) {
        this.outer = requireNonNull(outer);
        this.name = requireNonNull(name);
    }

    @Override
    public String getNonGenericClassName() {
        return outer.getNonGenericClassName() + '$' + name;
    }

    @Override
    public <C, R> R accept(JavaTypeVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public JavaType resolve(TypeEnvironment env) {
        return new JavaInnerClassType(outer.resolve(env), name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaInnerClassType that = (JavaInnerClassType) o;

        return outer.equals(that.outer)
                && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outer, name);
    }

    @Override
    public String toString() {
        return outer + "." + name;
    }
}
