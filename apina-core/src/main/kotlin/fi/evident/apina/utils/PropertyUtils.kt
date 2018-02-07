package fi.evident.apina.utils

fun propertyNameForGetter(getterName: String): String =
    when {
        getterName.startsWith("get") -> getterName.removePrefix("get").decapitalize()
        getterName.startsWith("is") -> getterName.removePrefix("is").decapitalize()
        else -> throw IllegalArgumentException("not a valid name for getter $getterName")
    }
