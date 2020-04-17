package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import java.util.*

class DiscriminatedUnionDefinition(
    val type: ApiTypeName,
    val discriminator: String) {

    private val _types = TreeMap<String, ApiType.Class>()

    val types: SortedMap<String, ApiType.Class>
        get() = _types

    fun addType(discriminator: String, type: ApiType.Class) {
        val old = types.putIfAbsent(discriminator, type)
        check(old == null) { "Tried to add duplicated case '$discriminator' to discriminated union '$type" }
    }

    override fun toString() = type.toString()
}
