package fi.evident.apina.utils

import fi.evident.apina.java.model.JavaMethod

val JavaMethod.propertyName: String
    get() = propertyNameForGetter(name)

fun propertyNameForGetter(getterName: String): String =
    when {
        getterName.startsWith("get") -> getterName.removePrefix("get").decapitalize()
        getterName.startsWith("is") -> getterName.removePrefix("is").decapitalize()
        else -> error("not a valid name for getter $getterName")
    }
