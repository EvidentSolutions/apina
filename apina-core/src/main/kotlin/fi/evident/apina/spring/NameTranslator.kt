package fi.evident.apina.spring

import java.lang.Math.max

/**
 * Translates various Java names to api names.
 */
internal object NameTranslator {

    @JvmStatic
    fun translateEndpointGroupName(name: String) = translateClassName(name).removeSuffix("Controller")

    @JvmStatic
    fun translateClassName(name: String): String {
        val lastDot = max(name.lastIndexOf('.'), name.lastIndexOf('$'))
        return if (lastDot != -1)
            name.substring(lastDot + 1)
        else
            name
    }
}
