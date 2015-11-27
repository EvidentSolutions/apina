package fi.evident.apina.java.model.type;

/**
 * Base class for all Java types, like {@link java.lang.reflect.Type}.
 */
public abstract class JavaType {

    /** Package private constructor so that subclasses are not defined outside this package */
    JavaType() { }

    public JavaBasicType toBasicType() {
        throw new ClassCastException("can't cast " + getClass().getName() + " to JavaBasicType");
    }

    public abstract String getNonGenericClassName();

    public boolean isVoid() {
        return false;
    }

    public abstract <C,R> R accept(JavaTypeVisitor<C,R> visitor, C ctx);

    public abstract JavaType resolve(TypeEnvironment env);

    // Force subclass to implement equals, hashCode and toString since we really want them for all types

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
