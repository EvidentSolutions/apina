package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiClassType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

/**
 * Represents the whole API of the program: i.e. all {@link EndpointGroup}s.
 */
public final class ApiDefinition {

    private final Collection<EndpointGroup> endpointGroups = new ArrayList<>();
    private final Map<ApiClassType, ClassDefinition> classDefinitions = new TreeMap<>();

    public Collection<EndpointGroup> getEndpointGroups() {
        return unmodifiableCollection(endpointGroups);
    }

    public void addEndpointGroups(EndpointGroup group) {
        endpointGroups.add(requireNonNull(group));
    }

    public boolean containsClassType(ApiClassType classType) {
        return classDefinitions.containsKey(classType);
    }

    public void addClassDefinition(ClassDefinition classDefinition) {
        ClassDefinition old = classDefinitions.putIfAbsent(classDefinition.getType(), classDefinition);
        if (old != null)
            throw new IllegalArgumentException("tried to add class-definition twice: " + classDefinition.getType());
    }

    public Collection<ClassDefinition> getClassDefinitions() {
        return unmodifiableCollection(classDefinitions.values());
    }
}
