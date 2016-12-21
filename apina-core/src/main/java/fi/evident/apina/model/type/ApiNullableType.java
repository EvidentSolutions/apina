package fi.evident.apina.model.type;

import java.util.Objects;

public final class ApiNullableType extends ApiType {

    private final ApiType type;

    public ApiNullableType(ApiType type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public String typeRepresentation() {
        return type.typeRepresentation() + " | null";
    }

    @Override
    public ApiType unwrapNullable() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiNullableType that = (ApiNullableType) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
