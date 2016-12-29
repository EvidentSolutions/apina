package fi.evident.apina.model.parameters

import fi.evident.apina.model.type.ApiType

class EndpointPathVariableParameter(name: String, pathVariable: String?, type: ApiType) : EndpointParameter(name, type) {

    val pathVariable: String = pathVariable ?: name

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("@PathVariable")

        if (pathVariable != name)
            sb.append("(\"").append(pathVariable).append("\")")

        sb.append(' ').append(type).append(' ').append(name)
        return sb.toString()
    }
}
