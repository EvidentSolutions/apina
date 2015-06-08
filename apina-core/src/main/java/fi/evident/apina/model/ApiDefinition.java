package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiArrayType;
import fi.evident.apina.model.type.ApiBlackBoxType;
import fi.evident.apina.model.type.ApiClassType;
import fi.evident.apina.model.type.ApiType;

import java.util.*;
import java.util.stream.Stream;

import static fi.evident.apina.utils.CollectionUtils.concat;
import static fi.evident.apina.utils.CollectionUtils.optionalToStream;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Represents the whole API of the program: i.e. all {@link EndpointGroup}s.
 */
public final class ApiDefinition {

    private final Collection<EndpointGroup> endpointGroups = new ArrayList<>();
    private final Map<ApiClassType, ClassDefinition> classDefinitions = new TreeMap<>();
    private final Set<ApiBlackBoxType> blackBoxTypes = new LinkedHashSet<>();

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

    public int getEndpointGroupCount() {
        return endpointGroups.size();
    }

    public int getEndpointCount() {
        return endpointGroups.stream()
                .mapToInt(EndpointGroup::getEndpointCount)
                .sum();
    }

    public int getClassDefinitionCount() {
        return classDefinitions.size();
    }

    public Collection<ApiType> getAllBlackBoxClasses() {
        return concat(blackBoxTypes, getUnknownTypeReferences());
    }

    /**
     * Returns all class types that refer to unknown classes.
     */
    public Set<ApiClassType> getUnknownTypeReferences() {
        Stream<ApiType> resultTypes = endpointGroups.stream()
                .flatMap(e -> e.getEndpoints().stream())
                .flatMap(e -> optionalToStream(e.getResponseBody()));

        Stream<ApiType> propertyTypes = classDefinitions.values().stream()
                .flatMap(c -> c.getProperties().stream())
                .map(PropertyDefinition::getType);

        return Stream.concat(resultTypes, propertyTypes)
                .flatMap(ApiDefinition::referencedClassTypes)
                .filter(c -> !containsClassType(c))
                .collect(toSet());
    }

    private static Stream<ApiClassType> referencedClassTypes(ApiType type) {
        if (type instanceof ApiClassType) {
            return Stream.of((ApiClassType) type);
        } else if (type instanceof ApiArrayType) {
            return referencedClassTypes(((ApiArrayType) type).getElementType());
        } else {
            return Stream.empty();
        }
    }

    public void addBlackBox(ApiBlackBoxType type) {
        blackBoxTypes.add(requireNonNull(type));
    }
}
