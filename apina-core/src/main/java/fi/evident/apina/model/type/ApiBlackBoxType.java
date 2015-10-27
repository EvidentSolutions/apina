package fi.evident.apina.model.type;

import static java.util.Objects.requireNonNull;

public final class ApiBlackBoxType extends ApiType {

    private final ApiTypeName name;

    public ApiBlackBoxType(ApiTypeName name) {
        this.name = requireNonNull(name);
    }

    public ApiTypeName getName() {
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
    public String typeRepresentation() {
        return name.toString();
    }
}
