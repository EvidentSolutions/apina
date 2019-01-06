package fi.evident.apina.model

import fi.evident.apina.model.type.ApiTypeName
import java.util.*

class ClassDefinition(val type: ApiTypeName) {

    private val _properties = TreeMap<String, PropertyDefinition>()

    val properties: Collection<PropertyDefinition>
        get() = _properties.values

    fun addProperty(property: PropertyDefinition) {
        val old = _properties.putIfAbsent(property.name, property)
        if (old != null)
            throw IllegalArgumentException("duplicate property: ${property.name}")
    }

    fun hasProperty(name: String) = name in _properties

    override fun toString() = type.toString()
}
