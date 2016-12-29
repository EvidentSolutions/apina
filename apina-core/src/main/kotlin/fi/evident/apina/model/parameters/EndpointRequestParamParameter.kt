package fi.evident.apina.model.parameters

import fi.evident.apina.model.type.ApiType

class EndpointRequestParamParameter(name: String, queryParameter: String?, type: ApiType) : EndpointParameter(name, type) {

    val queryParameter: String = queryParameter ?: name

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("@RequestParam")

        if (queryParameter != name)
            sb.append("(\"").append(queryParameter).append("\")")

        sb.append(' ').append(type).append(' ').append(name)
        return sb.toString()
    }
}
