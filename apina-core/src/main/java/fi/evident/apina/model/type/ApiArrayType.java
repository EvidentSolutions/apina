package fi.evident.apina.model.type;

import static java.util.Objects.requireNonNull;

public final class ApiArrayType extends ApiType {

    private final ApiType elementType;

    public ApiArrayType(ApiType elementType) {
        this.elementType = requireNonNull(elementType);
    }

    public ApiType getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return elementType + "[]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiArrayType that = (ApiArrayType) o;

        return elementType.equals(that.elementType);
    }

    @Override
    public int hashCode() {
        return elementType.hashCode();
    }
}
