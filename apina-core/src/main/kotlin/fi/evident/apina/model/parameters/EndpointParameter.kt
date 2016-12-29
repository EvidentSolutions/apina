package fi.evident.apina.model.parameters

import fi.evident.apina.model.type.ApiType

abstract class EndpointParameter internal constructor(val name: String, val type: ApiType) {
    abstract override fun toString(): String
}
