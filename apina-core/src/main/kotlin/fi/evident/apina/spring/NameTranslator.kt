package fi.evident.apina.spring

import java.lang.Math.max

fun translateEndpointGroupName(name: String) = translateClassName(name).removeSuffix("Controller")

fun translateClassName(name: String): String {
    val lastSeparator = max(name.lastIndexOf('.'), name.lastIndexOf('$'))
    return if (lastSeparator != -1)
        name.substring(lastSeparator + 1)
    else
        name
}
