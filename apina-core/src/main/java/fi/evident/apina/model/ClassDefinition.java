package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiTypeName;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

public final class ClassDefinition {

    private final ApiTypeName type;

    private final HashMap<String, PropertyDefinition> properties = new LinkedHashMap<>();

    public ClassDefinition(ApiTypeName type) {
        this.type = requireNonNull(type);
    }

    public ApiTypeName getType() {
        return type;
    }

    public Collection<PropertyDefinition> getProperties() {
        return unmodifiableCollection(properties.values());
    }

    public void addProperty(PropertyDefinition property) {
        PropertyDefinition old = properties.putIfAbsent(property.getName(), property);
        if (old != null)
            throw new IllegalArgumentException("duplicate property: " + property.getName());
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(requireNonNull(name));
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
