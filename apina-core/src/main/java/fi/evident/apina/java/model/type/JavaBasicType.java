package fi.evident.apina.java.model.type;

import static java.util.Objects.requireNonNull;

/**
 * Represents a raw Java type, like {@link Class}.
 */
public final class JavaBasicType extends JavaType {

    public static JavaBasicType BOOLEAN = new JavaBasicType("boolean");
    public static JavaBasicType INT = new JavaBasicType("int");
    public static JavaBasicType SHORT = new JavaBasicType("short");
    public static JavaBasicType LONG = new JavaBasicType("long");
    public static JavaBasicType FLOAT = new JavaBasicType("float");
    public static JavaBasicType DOUBLE = new JavaBasicType("double");
    public static JavaBasicType VOID = new JavaBasicType("void");

    public final String name;

    public JavaBasicType(String name) {
        this.name = requireNonNull(name);
    }

    public JavaBasicType(Class<?> cl) {
        this.name = requireNonNull(cl.getName());
    }

    public String getName() {
        return name;
    }

    @Override
    public String getNonGenericClassName() {
        return name;
    }

    @Override
    public <C, R> R accept(JavaTypeVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public JavaType resolve(TypeEnvironment env) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaBasicType that = (JavaBasicType) o;

        return name.equals(that.name);
    }

    @Override
    public boolean isWide() {
        return this.equals(LONG) || this.equals(DOUBLE);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public JavaBasicType toBasicType() {
        return this;
    }

    public boolean isPrimitiveNumber() {
        return this.equals(INT)
                || this.equals(SHORT)
                || this.equals(LONG)
                || this.equals(FLOAT)
                || this.equals(DOUBLE);
    }

    @Override
    public boolean isVoid() {
        return name.equals("void");
    }
}
