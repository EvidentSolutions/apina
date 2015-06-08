package fi.evident.apina.model.type;

import static java.util.Objects.requireNonNull;

public final class ApiBlackBoxType extends ApiType {

    private final String name;

    public ApiBlackBoxType(String name) {
        this.name = requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiBlackBoxType that = (ApiBlackBoxType) o;

        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
