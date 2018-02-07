package fi.evident.apina.model.type

import java.util.*

sealed class ApiType {

    abstract fun typeRepresentation(): String
    open fun unwrapNullable(): ApiType = this

    override fun toString() = typeRepresentation()
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    class Array(val elementType: ApiType) : ApiType() {
        override fun typeRepresentation() = "$elementType[]"
        override fun equals(other: Any?) = other is Array && elementType == other.elementType
        override fun hashCode() = elementType.hashCode()
    }

    class BlackBox(val name: ApiTypeName) : ApiType() {
        override fun typeRepresentation() = name.toString()
        override fun equals(other: Any?) = other is BlackBox && name == other.name
        override fun hashCode() = name.hashCode()
    }

    /**
     * Represents class types.
     */
    class Class(val name: ApiTypeName) : ApiType(), Comparable<Class> {

        constructor(name: String): this(ApiTypeName(name))
        override fun typeRepresentation() = name.toString()
        override fun equals(other: Any?) = other is Class && name == other.name
        override fun hashCode() = name.hashCode()
        override fun compareTo(other: Class) = name.compareTo(other.name)
    }

    class Dictionary(private val valueType: ApiType) : ApiType() {
        override fun typeRepresentation() = "Dictionary<$valueType>"
        override fun equals(other: Any?) = other is Dictionary && valueType == other.valueType
        override fun hashCode() = Objects.hash(valueType)
    }

    class Nullable(val type: ApiType) : ApiType() {
        override fun typeRepresentation() = type.typeRepresentation() + " | null"
        override fun unwrapNullable() = type
        override fun equals(other: Any?) = other is Nullable && type == other.type
        override fun hashCode() = Objects.hash(type)
    }

    class Primitive private constructor(private val name: String) : ApiType() {

        override fun typeRepresentation() = name
        override fun hashCode() = System.identityHashCode(this)
        override fun equals(other: Any?) = other === this

        companion object {
            val ANY: ApiType = Primitive("any")
            val STRING: ApiType = Primitive("string")
            val BOOLEAN: ApiType = Primitive("boolean")
            val NUMBER: ApiType = Primitive("number")
            val VOID: ApiType = Primitive("void")
        }
    }
}
