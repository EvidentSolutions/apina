package fi.evident.apina.model.type;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class ApiTypeName implements Comparable<ApiTypeName> {

    private final String name;

    public ApiTypeName(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiTypeName that = (ApiTypeName) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull ApiTypeName o) {
        return name.compareTo(o.name);
    }
}
