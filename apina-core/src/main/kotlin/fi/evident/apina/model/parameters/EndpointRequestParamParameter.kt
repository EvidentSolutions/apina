package fi.evident.apina.model.parameters

import fi.evident.apina.model.type.ApiType

class EndpointRequestParamParameter(name: String, queryParameter: String?, type: ApiType) : EndpointParameter(name, type) {

    val queryParameter: String = queryParameter ?: name

    override fun toString(): String = buildString {
        append("@RequestParam")

        if (queryParameter != name)
            append("(\"").append(queryParameter).append("\")")

        append(' ').append(type).append(' ').append(name)
    }
}
