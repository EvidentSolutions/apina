package fi.evident.apina.model;

import static java.util.Objects.requireNonNull;

/**
 * Qualified name for a type.
 */
public final class TypeName {

    private final String name;

    public TypeName(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeName typeName = (TypeName) o;

        return name.equals(typeName.name);
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
