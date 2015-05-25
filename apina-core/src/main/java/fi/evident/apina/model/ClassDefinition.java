package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiClassType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class ClassDefinition {

    private final ApiClassType type;

    private final List<PropertyDefinition> properties = new ArrayList<>();

    public ClassDefinition(ApiClassType type) {
        this.type = requireNonNull(type);
    }

    public ApiClassType getType() {
        return type;
    }

    public List<PropertyDefinition> getProperties() {
        return unmodifiableList(properties);
    }

    public void addProperty(PropertyDefinition property) {
        properties.add(requireNonNull(property));
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
