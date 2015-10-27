package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiArrayType;
import fi.evident.apina.model.type.ApiClassType;
import fi.evident.apina.model.type.ApiType;
import fi.evident.apina.model.type.ApiTypeName;

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
    private final Map<ApiTypeName, ClassDefinition> classDefinitions = new TreeMap<>();
    private final Map<ApiTypeName, EnumDefinition> enumDefinitions = new TreeMap<>();
    private final Set<ApiTypeName> blackBoxTypes = new LinkedHashSet<>();

    public Collection<EndpointGroup> getEndpointGroups() {
        return unmodifiableCollection(endpointGroups);
    }

    public void addEndpointGroups(EndpointGroup group) {
        endpointGroups.add(requireNonNull(group));
    }

    public boolean containsType(ApiTypeName typeName) {
        return classDefinitions.containsKey(typeName) || enumDefinitions.containsKey(typeName);
    }

    public void addClassDefinition(ClassDefinition classDefinition) {
        verifyTypeDoesNotExist(classDefinition.getType());

        classDefinitions.put(classDefinition.getType(), classDefinition);
    }

    public Collection<ClassDefinition> getClassDefinitions() {
        return unmodifiableCollection(classDefinitions.values());
    }

    public void addEnumDefinition(EnumDefinition enumDefinition) {
        verifyTypeDoesNotExist(enumDefinition.getType());
        enumDefinitions.put(enumDefinition.getType(), enumDefinition);
    }

    private void verifyTypeDoesNotExist(ApiTypeName type) {
        if (containsType(type))
            throw new IllegalArgumentException("tried to add type-definition twice: " + type);
    }

    public Collection<EnumDefinition> getEnumDefinitions() {
        return unmodifiableCollection(enumDefinitions.values());
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

    public int getEnumDefinitionCount() {
        return enumDefinitions.size();
    }

    public Collection<ApiTypeName> getAllBlackBoxClasses() {
        return concat(blackBoxTypes, getUnknownTypeReferences());
    }

    /**
     * Returns all class types that refer to unknown classes.
     */
    public Set<ApiTypeName> getUnknownTypeReferences() {
        Stream<ApiType> resultTypes = endpointGroups.stream()
                .flatMap(e -> e.getEndpoints().stream())
                .flatMap(e -> optionalToStream(e.getResponseBody()));

        Stream<ApiType> propertyTypes = classDefinitions.values().stream()
                .flatMap(c -> c.getProperties().stream())
                .map(PropertyDefinition::getType);

        return Stream.concat(resultTypes, propertyTypes)
                .flatMap(ApiDefinition::referencedClassTypes)
                .filter(c -> !containsType(c))
                .collect(toSet());
    }

    private static Stream<ApiTypeName> referencedClassTypes(ApiType type) {
        if (type instanceof ApiClassType) {
            return Stream.of(((ApiClassType) type).getName());
        } else if (type instanceof ApiArrayType) {
            return referencedClassTypes(((ApiArrayType) type).getElementType());
        } else {
            return Stream.empty();
        }
    }

    public void addBlackBox(ApiTypeName type) {
        blackBoxTypes.add(requireNonNull(type));
    }
}
