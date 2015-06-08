package fi.evident.apina.model.type;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class ApiDictionaryType extends ApiType {

    private final ApiType keyType;
    private final ApiType valueType;

    public ApiDictionaryType(ApiType keyType, ApiType valueType) {
        this.keyType = requireNonNull(keyType);
        this.valueType = requireNonNull(valueType);
    }

    public ApiType getKeyType() {
        return keyType;
    }

    public ApiType getValueType() {
        return valueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiDictionaryType that = (ApiDictionaryType) o;

        return keyType.equals(that.keyType)
                && valueType.equals(that.valueType);

    }

    @Override
    public int hashCode() {
        return Objects.hash(keyType, valueType);
    }

    @Override
    public String toString() {
        return "IDictionary<" + keyType + ", " + valueType + '>';
    }
}
