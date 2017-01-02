package fi.evident.apina.spring

import java.lang.Math.max

fun translateEndpointGroupName(name: String) = translateClassName(name).removeSuffix("Controller")

fun translateClassName(name: String): String {
    val lastDot = max(name.lastIndexOf('.'), name.lastIndexOf('$'))
    return if (lastDot != -1)
        name.substring(lastDot + 1)
    else
        name
}
