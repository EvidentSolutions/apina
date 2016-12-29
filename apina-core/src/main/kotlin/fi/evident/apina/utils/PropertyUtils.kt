package fi.evident.apina.utils

fun propertyNameForGetter(getterName: String): String =
    if (getterName.startsWith("get"))
        getterName.removePrefix("get").decapitalize()
    else if (getterName.startsWith("is"))
        getterName.removePrefix("is").decapitalize()
    else
        throw IllegalArgumentException("not a valid name for getter $getterName")
