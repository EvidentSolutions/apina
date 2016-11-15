package fi.evident.apina.model.type;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class ApiDictionaryType extends ApiType {

    private final ApiType valueType;

    public ApiDictionaryType(ApiType valueType) {
        this.valueType = requireNonNull(valueType);
    }

    public ApiType getValueType() {
        return valueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiDictionaryType that = (ApiDictionaryType) o;

        return valueType.equals(that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueType);
    }

    @Override
    public String typeRepresentation() {
        return "Dictionary<" + valueType + '>';
    }
}
