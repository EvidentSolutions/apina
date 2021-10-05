package fi.evident.apina.utils

fun propertyNameForGetter(getterName: String): String =
    when {
        // Value/inline classes result in name suffixed with hash
        getterName.contains("-") -> propertyNameForSimpleGetter(getterName.substringBefore('-'))
        else -> propertyNameForSimpleGetter(getterName)
    }

private fun propertyNameForSimpleGetter(getterName: String): String =
    when {
        getterName.startsWith("get") -> getterName.removePrefix("get").decapitalize()
        getterName.startsWith("is") -> getterName.removePrefix("is").decapitalize()
        else -> error("not a valid name for getter $getterName")
    }
