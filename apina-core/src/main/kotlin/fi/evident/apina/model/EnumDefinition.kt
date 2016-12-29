package fi.evident.apina.model

import fi.evident.apina.model.type.ApiTypeName

class EnumDefinition(val type: ApiTypeName, val constants: List<String>) {
    override fun toString() = type.toString()
}
