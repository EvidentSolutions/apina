package fi.evident.apina.utils

import java.util.Locale

fun propertyNameForGetter(getterName: String): String =
    when {
        // Value/inline classes result in name suffixed with hash
        getterName.contains("-") -> propertyNameForSimpleGetter(getterName.substringBefore('-'))
        else -> propertyNameForSimpleGetter(getterName)
    }

private fun propertyNameForSimpleGetter(getterName: String): String =
    when {
        getterName.startsWith("get") -> getterName.removePrefix("get")
            .replaceFirstChar { it.lowercase(Locale.getDefault()) }
        getterName.startsWith("is") -> getterName.removePrefix("is")
            .replaceFirstChar { it.lowercase(Locale.getDefault()) }
        else -> error("not a valid name for getter $getterName")
    }
