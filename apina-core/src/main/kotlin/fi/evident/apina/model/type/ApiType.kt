package fi.evident.apina.model.type

sealed class ApiType {

    abstract fun toTypeScript(): String
    abstract fun toSwift(): String
    open fun unwrapNullable(): ApiType = this

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    data class Array(val elementType: ApiType) : ApiType() {
        override fun toTypeScript() = "${elementType.toTypeScript()}[]"
        override fun toSwift() = "[${elementType.toSwift()}]"
    }

    data class BlackBox(val name: ApiTypeName) : ApiType() {
        override fun toTypeScript() = name.name
        override fun toSwift() = name.name
    }

    /**
     * Represents class types.
     */
    data class Class(val name: ApiTypeName) : ApiType(), Comparable<Class> {

        constructor(name: String): this(ApiTypeName(name))
        override fun toTypeScript() = name.name
        override fun toSwift() = name.name
        override fun compareTo(other: Class) = name.compareTo(other.name)
    }

    data class Dictionary(private val valueType: ApiType) : ApiType() {
        override fun toTypeScript() = "Dictionary<${valueType.toTypeScript()}>"
        override fun toSwift() = "[String: ${valueType.toSwift()}]"
    }

    data class Nullable(val type: ApiType) : ApiType() {
        override fun toTypeScript() = type.toTypeScript() + " | null"
        override fun toSwift() = type.toSwift() + "?"
        override fun unwrapNullable() = type
    }

    class Primitive private constructor(
        private val typescriptName: String,
        private val swiftName: String) : ApiType() {

        override fun toString() = typescriptName
        override fun toTypeScript() = typescriptName
        override fun toSwift() = swiftName
        override fun hashCode() = System.identityHashCode(this)
        override fun equals(other: Any?) = other === this

        companion object {
            val ANY: ApiType = Primitive(typescriptName = "any", swiftName = "Any")
            val STRING: ApiType = Primitive(typescriptName = "string", swiftName = "String")
            val BOOLEAN: ApiType = Primitive(typescriptName = "boolean", swiftName = "Bool")
            val INTEGER: ApiType = Primitive("number", swiftName = "Int")
            val FLOAT: ApiType = Primitive(typescriptName = "number", swiftName = "Float")
            val VOID: ApiType = Primitive(typescriptName = "void", swiftName = "Void")
        }
    }
}
