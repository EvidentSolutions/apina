package fi.evident.apina.java.model.type;

import static java.util.Objects.requireNonNull;

public final class JavaArrayType extends JavaType {

    private final JavaType elementType;

    public JavaArrayType(JavaType elementType) {
        this.elementType = requireNonNull(elementType);
    }

    public JavaType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaArrayType that = (JavaArrayType) o;

        return elementType.equals(that.elementType);
    }

    @Override
    public int hashCode() {
        return elementType.hashCode();
    }

    @Override
    public String toString() {
        return elementType + "[]";
    }
}
