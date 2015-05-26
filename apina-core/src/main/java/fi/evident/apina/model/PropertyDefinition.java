package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiType;

import static java.util.Objects.requireNonNull;

public final class PropertyDefinition {

    private final String name;

    private final ApiType type;

    public PropertyDefinition(String name, ApiType type) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
    }

    public String getName() {
        return name;
    }

    public ApiType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
