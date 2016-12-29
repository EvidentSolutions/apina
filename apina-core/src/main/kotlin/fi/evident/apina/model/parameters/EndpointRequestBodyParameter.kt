package fi.evident.apina.model.parameters

import fi.evident.apina.model.type.ApiType

class EndpointRequestBodyParameter(name: String, type: ApiType) : EndpointParameter(name, type) {

    override fun toString() = "@RequestBody $type $name"
}
