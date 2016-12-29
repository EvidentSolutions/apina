package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import java.util.*

/**
 * Represents the whole API of the program: i.e. all [EndpointGroup]s.
 */
class ApiDefinition {

    private val _endpointGroups = ArrayList<EndpointGroup>()
    private val _classDefinitions = TreeMap<ApiTypeName, ClassDefinition>()
    private val _enumDefinitions = TreeMap<ApiTypeName, EnumDefinition>()
    private val blackBoxTypes = LinkedHashSet<ApiTypeName>()

    val endpointGroups: Collection<EndpointGroup>
        get() = _endpointGroups

    val classDefinitions: Collection<ClassDefinition>
        get() = _classDefinitions.values

    val enumDefinitions: Collection<EnumDefinition>
        get() = _enumDefinitions.values

    fun addEndpointGroups(group: EndpointGroup) {
        _endpointGroups.add(group)
    }

    fun containsType(typeName: ApiTypeName) =
            _classDefinitions.containsKey(typeName) || _enumDefinitions.containsKey(typeName)

    fun addClassDefinition(classDefinition: ClassDefinition) {
        verifyTypeDoesNotExist(classDefinition.type)

        _classDefinitions.put(classDefinition.type, classDefinition)
    }

    fun addEnumDefinition(enumDefinition: EnumDefinition) {
        verifyTypeDoesNotExist(enumDefinition.type)
        _enumDefinitions.put(enumDefinition.type, enumDefinition)
    }

    private fun verifyTypeDoesNotExist(type: ApiTypeName) {
        if (containsType(type))
            throw IllegalArgumentException("tried to add type-definition twice: " + type)
    }

    val endpointGroupCount: Int
        get() = _endpointGroups.size

    val endpointCount: Int
        get() = _endpointGroups.sumBy { it.endpointCount }

    val classDefinitionCount: Int
        get() = _classDefinitions.size

    val enumDefinitionCount: Int
        get() = _enumDefinitions.size

    val allBlackBoxClasses: Collection<ApiTypeName>
        get() = blackBoxTypes + unknownTypeReferences

    /**
     * Returns all class types that refer to unknown classes.
     */
    val unknownTypeReferences: Set<ApiTypeName>
        get() {
            val resultTypes = _endpointGroups.asSequence()
                    .flatMap { it.endpoints.asSequence() }
                    .mapNotNull{ it.responseBody }

            val propertyTypes = _classDefinitions.values.asSequence()
                    .flatMap { it.properties.asSequence() }
                    .map { it.type }

            return (resultTypes + propertyTypes)
                    .flatMap { referencedClassTypes(it) }
                    .filterNot { containsType(it) }
                    .toSet()
        }

    private fun referencedClassTypes(type: ApiType): Sequence<ApiTypeName> = when (type) {
        is ApiType.Class -> sequenceOf(type.name)
        is ApiType.Array -> referencedClassTypes(type.elementType)
        else -> emptySequence()
    }

    fun addBlackBox(type: ApiTypeName) {
        blackBoxTypes.add(type)
    }
}
