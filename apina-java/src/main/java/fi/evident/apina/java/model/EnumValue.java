package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;

import static java.util.Objects.requireNonNull;

public final class EnumValue {

    private final JavaBasicType enumType;

    private final String constant;

    public EnumValue(JavaBasicType enumType, String constant) {
        this.enumType = requireNonNull(enumType);
        this.constant = requireNonNull(constant);
    }

    @Override
    public String toString() {
        return enumType + "." + constant;
    }
}
