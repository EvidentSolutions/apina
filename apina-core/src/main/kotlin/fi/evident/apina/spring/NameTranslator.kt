package fi.evident.apina.spring

import kotlin.math.max

class NameTranslator {

    private val registeredClassNames = mutableMapOf<String, String>()

    fun translateEndpointGroupName(name: String) = translateClassName(name).removeSuffix("Controller")

    fun translateClassName(name: String): String {
        val registered = registeredClassNames[name]
        if (registered != null)
            return registered

        val lastSeparator = max(name.lastIndexOf('.'), name.lastIndexOf('$'))
        return if (lastSeparator != -1)
            name.substring(lastSeparator + 1)
        else
            name
    }

    fun registerClassName(fullyQualifiedName: String, translatedName: String) {
        registeredClassNames[fullyQualifiedName] = translatedName
    }
}
