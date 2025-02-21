package fi.evident.apina.model.type

import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.output.ts.toTypeScript

sealed interface ApiType {

    fun unwrapNullable(): ApiType = this
    fun nullable(): Nullable = Nullable(this)
    fun toDescription() = toTypeScript(OptionalTypeMode.NULL) // arbitrary selection, but works for us

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    data class Array(val elementType: ApiType) : ApiType

    data class BlackBox(val name: ApiTypeName) : ApiType

    /**
     * Represents class types.
     */
    data class Class(val name: ApiTypeName) : ApiType, Comparable<Class> {

        constructor(name: String) : this(ApiTypeName(name))

        override fun compareTo(other: Class) = name.compareTo(other.name)
    }

    data class Dictionary(val valueType: ApiType) : ApiType

    data class Nullable(val type: ApiType) : ApiType {
        override fun unwrapNullable() = type
        override fun nullable(): Nullable = this
    }

    enum class Primitive : ApiType {

        ANY, STRING, BOOLEAN, INTEGER, FLOAT, VOID;

        override fun toString() = name.lowercase()

        companion object {

            fun forName(name: String): ApiType = valueOf(name.uppercase())
        }
    }
}
