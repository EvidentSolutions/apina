package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType

class EnumValue(private val enumType: JavaType.Basic, val constant: String) {

    override fun toString() = "$enumType $constant"
}
