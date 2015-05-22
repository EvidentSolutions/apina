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
}
