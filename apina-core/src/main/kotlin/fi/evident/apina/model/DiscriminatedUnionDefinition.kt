package fi.evident.apina.model

import fi.evident.apina.model.type.ApiTypeName
import java.util.*

class DiscriminatedUnionDefinition(
    val type: ApiTypeName,
    val discriminator: String) {

    private val _types = TreeMap<String, ClassDefinition>()

    val types: SortedMap<String, ClassDefinition>
        get() = _types

    fun addType(discriminator: String, type: ClassDefinition) {
        val old = types.putIfAbsent(discriminator, type)
        check(old == null) { "Tried to add duplicated case '$discriminator' to discriminated union '$type" }
    }

    override fun toString() = type.toString()
}
