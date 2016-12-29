package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType

class PropertyDefinition(val name: String, val type: ApiType) {
    override fun toString() = "$type $name"
}
