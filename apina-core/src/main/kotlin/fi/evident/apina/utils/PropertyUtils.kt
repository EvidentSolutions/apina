package fi.evident.apina.utils

fun propertyNameForGetter(getterName: String): String =
    // Value/inline classes result in name suffixed with hash
    propertyNameForSimpleGetter(getterName.substringBefore('-'))

private fun propertyNameForSimpleGetter(getterName: String): String =
    when {
        getterName.startsWith("get") -> getterName.removePrefix("get").replaceFirstChar { it.lowercase() }
        getterName.startsWith("is") -> getterName.removePrefix("is").replaceFirstChar { it.lowercase() }
        else -> error("not a valid name for getter $getterName")
    }
