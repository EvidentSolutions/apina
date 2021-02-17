package fi.evident.apina.model

import fi.evident.apina.model.parameters.EndpointParameter
import fi.evident.apina.model.parameters.EndpointPathVariableParameter
import fi.evident.apina.model.parameters.EndpointRequestBodyParameter
import fi.evident.apina.model.parameters.EndpointRequestParamParameter
import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.type.ApiType
import java.util.*

/**
 * API endpoint reachable at an URL using given method and parameters. An example
 * of an endpoint is a single method on a controller.
 * Represents an single endpoint (e.g. a method in a controller)
 */
class Endpoint(
    /** Name of the original source element that specifies this endpoint  */
    val name: String,

    /** URI template for the endpoint  */
    val uriTemplate: URITemplate,
    val responseBody: ApiType?,
    val generateUrlMethod: Boolean,
    val optionalTypeMode: OptionalTypeMode
) {

    private val _parameters = ArrayList<EndpointParameter>()

    /** HTTP method for accessing the endpoint  */
    var method = HTTPMethod.GET

    val parameters: List<EndpointParameter>
        get() = _parameters

    val urlParameters: List<EndpointParameter>
        get() = _parameters.filter { it !is EndpointRequestBodyParameter }

    fun addParameter(parameter: EndpointParameter) {
        _parameters += parameter
    }

    val requestBody: EndpointRequestBodyParameter?
        get() = _parameters.asSequence().filterIsInstance<EndpointRequestBodyParameter>().firstOrNull()

    val pathVariables: List<EndpointPathVariableParameter>
        get() = _parameters.filterIsInstance<EndpointPathVariableParameter>()

    val requestParameters: List<EndpointRequestParamParameter>
        get() = _parameters.filterIsInstance<EndpointRequestParamParameter>()

    override fun toString(): String = String.format("%s %s(%s): %s %s",
            responseBody?.toTypeScript(optionalTypeMode) ?: "void",
            name,
            _parameters.joinToString(", "),
            method,
            uriTemplate)
}
