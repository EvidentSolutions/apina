package fi.evident.apina.model.type

sealed class ApiType {

    abstract fun toTypeScript(): String
    open fun unwrapNullable(): ApiType = this

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    data class Array(val elementType: ApiType) : ApiType() {
        override fun toTypeScript() = "${elementType.toTypeScript()}[]"
    }

    data class BlackBox(val name: ApiTypeName) : ApiType() {
        override fun toTypeScript() = name.name
    }

    /**
     * Represents class types.
     */
    data class Class(val name: ApiTypeName) : ApiType(), Comparable<Class> {

        constructor(name: String): this(ApiTypeName(name))
        override fun toTypeScript() = name.name
        override fun compareTo(other: Class) = name.compareTo(other.name)
    }

    data class Dictionary(private val valueType: ApiType) : ApiType() {
        override fun toTypeScript() = "Dictionary<${valueType.toTypeScript()}>"
    }

    data class Nullable(val type: ApiType) : ApiType() {
        override fun toTypeScript() = type.toTypeScript() + " | null"
        override fun unwrapNullable() = type
    }

    class Primitive private constructor(private val name: String) : ApiType() {

        override fun toString() = name
        override fun toTypeScript() = name
        override fun hashCode() = System.identityHashCode(this)
        override fun equals(other: Any?) = other === this

        companion object {
            val ANY: ApiType = Primitive("any")
            val STRING: ApiType = Primitive("string")
            val BOOLEAN: ApiType = Primitive("boolean")
            val INTEGER: ApiType = Primitive("number")
            val FLOAT: ApiType = Primitive("number")
            val VOID: ApiType = Primitive("void")
        }
    }
}
