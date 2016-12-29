package fi.evident.apina.model.settings

import fi.evident.apina.model.type.ApiTypeName
import java.util.*

class ImportDefinition(val moduleName: String) {

    private val _types = TreeSet<ApiTypeName>()

    val types: Set<ApiTypeName>
        get() = _types

    fun addType(type: ApiTypeName) {
        _types.add(type)
    }
}
