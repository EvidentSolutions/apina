package fi.evident.apina.model

import java.util.*

/**
 * A group of related [Endpoint]s. As an example, web controllers are a group
 * of individual endpoints (methods).
 */
class EndpointGroup(val name: String,
                    /** Name of the original source element that specifies this endpoint group */
                    val originalName: String) {

    private val _endpoints = ArrayList<Endpoint>()

    fun addEndpoint(endpoint: Endpoint) {
        _endpoints += endpoint
    }

    val endpoints: Collection<Endpoint>
        get() = _endpoints

    override fun toString() = name

    val endpointCount: Int
        get() = _endpoints.size
}
