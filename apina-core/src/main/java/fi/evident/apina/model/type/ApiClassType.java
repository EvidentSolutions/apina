package fi.evident.apina.model.type;

import static java.util.Objects.requireNonNull;

/**
 * Represents class types.
 */
public final class ApiClassType extends ApiType {

    private final String name;

    public ApiClassType(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiClassType apiClassType = (ApiClassType) o;

        return name.equals(apiClassType.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
