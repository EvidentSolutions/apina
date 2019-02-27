package fi.evident.apina.model

import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import java.util.*
import java.util.Collections.unmodifiableCollection

class ClassDefinition(val type: ApiTypeName,
                      val variables: List<ApiType.Variable>) {

    private val _properties = TreeMap<String, PropertyDefinition>()
    private val _types = mutableSetOf<ApiType.Class>()

    val genericType = ApiType.Class(type, variables)

    val properties: Collection<PropertyDefinition>
        get() = _properties.values

    val types: Collection<ApiType>
        get() = unmodifiableCollection(_types)

    // FIXME: properties need to be per type
    fun addProperty(property: PropertyDefinition) {
        val old = _properties.putIfAbsent(property.name, property)
        if (old != null)
            throw IllegalArgumentException("duplicate property: ${property.name}")
    }

    fun addType(type: ApiType.Class) {
        _types.add(type)
    }

    fun hasProperty(name: String) = name in _properties

    fun toTypeScript(optionalTypeMode: OptionalTypeMode) = genericType.toTypeScript(optionalTypeMode)
    fun toSwift() = genericType.toSwift()

    override fun toString() = type.toString()
}
