package fi.evident.apina.model.type;

import static java.util.Objects.requireNonNull;

/**
 * Qualified name for a type.
 */
public final class ApiBasicType extends ApiType {

    private final String name;

    public ApiBasicType(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiBasicType apiBasicType = (ApiBasicType) o;

        return name.equals(apiBasicType.name);
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
