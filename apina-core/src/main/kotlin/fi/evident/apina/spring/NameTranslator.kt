package fi.evident.apina.spring

import kotlin.math.max

class NameTranslator {

    private val registeredClassNames = mutableMapOf<String, String>()

    fun translateEndpointGroupName(name: String) = translateClassName(name, qualifyNestedClasses = false).removeSuffix("Controller")

    /**
     * Translates a class name, optionally qualifying nested classes with their parent class name.
     *
     * @param name The fully qualified class name (e.g., "com.example.Foo$Bar")
     * @param qualifyNestedClasses Whether to include all parent class names in the result for nested classes
     * @return The translated name (e.g., "Bar" or "Foo_Bar_Baz" depending on parameters)
     */
    fun translateClassName(name: String, qualifyNestedClasses: Boolean): String {
        val registered = registeredClassNames[name]
        if (registered != null)
            return registered

        if (!qualifyNestedClasses || !isNestedClass(name)) {
            // Use simple name (just the last part after . or $)
            val lastSeparator = max(name.lastIndexOf('.'), name.lastIndexOf('$'))
            return if (lastSeparator != -1)
                name.substring(lastSeparator + 1)
            else
                name
        }

        // Extract all class name parts (excluding package) and join with underscores
        return extractClassNames(name).joinToString("_")
    }

    private fun isNestedClass(fullyQualifiedName: String): Boolean =
        fullyQualifiedName.contains('$')

    private fun extractClassNames(fullyQualifiedName: String): List<String> {
        // First, remove package prefix
        val lastDotIndex = fullyQualifiedName.lastIndexOf('.')
        val classNamePart = if (lastDotIndex != -1)
            fullyQualifiedName.substring(lastDotIndex + 1)
        else
            fullyQualifiedName

        // Split by '$' to get all nested class levels
        return classNamePart.split('$')
    }

    fun registerClassName(fullyQualifiedName: String, translatedName: String) {
        registeredClassNames[fullyQualifiedName] = translatedName
    }
}
