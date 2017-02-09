package fi.evident.apina.model.parameters

import fi.evident.apina.model.type.ApiType

class EndpointPathVariableParameter(name: String, pathVariable: String?, type: ApiType) : EndpointParameter(name, type) {

    val pathVariable: String = pathVariable ?: name

    override fun toString(): String = buildString {
        append("@PathVariable")

        if (pathVariable != name)
            append("(\"").append(pathVariable).append("\")")

        append(' ').append(type).append(' ').append(name)
    }
}
