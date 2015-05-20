package fi.evident.apina.spring.java.model;

import static java.util.Objects.requireNonNull;

public final class EnumValue {

    private final QualifiedName enumType;

    private final String constant;

    public EnumValue(QualifiedName enumType, String constant) {
        this.enumType = requireNonNull(enumType);
        this.constant = requireNonNull(constant);
    }

    @Override
    public String toString() {
        return enumType + "." + constant;
    }
}
