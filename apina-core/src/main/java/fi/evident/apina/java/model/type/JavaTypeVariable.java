package fi.evident.apina.java.model.type;

import static java.util.Objects.requireNonNull;

public final class JavaTypeVariable extends JavaType {

    private final String name;

    public JavaTypeVariable(String name) {
        this.name = requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public <C, R> R accept(JavaTypeVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaTypeVariable that = (JavaTypeVariable) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
