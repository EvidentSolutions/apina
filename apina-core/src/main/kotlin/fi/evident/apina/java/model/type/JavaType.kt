package fi.evident.apina.java.model.type

import java.util.*

/**
 * Base class for all Java types, like [java.lang.reflect.Type].
 */
sealed class JavaType {

    open fun toBasicType(): Basic =
            throw ClassCastException("can't cast ${javaClass.name} to JavaType.Basic")

    abstract val nonGenericClassName: String

    abstract fun resolve(env: TypeEnvironment): JavaType

    // Force subclass to implement equals, hashCode and toString since we really want them for all types

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    abstract override fun toString(): String

    open val isVoid: Boolean
        get() = false

    open val isWide: Boolean
        get() = false

    companion object {
        inline fun <reified T : Any> basic() = Basic(T::class.java)
    }

    /**
     * Represents a raw Java type, like [Class].
     */
    class Basic(val name: String) : JavaType() {

        constructor(cl: Class<*>): this(cl.name)

        override val nonGenericClassName: String
            get() = name

        override fun resolve(env: TypeEnvironment): JavaType = this

        override fun equals(other: Any?) = other is Basic && name == other.name
        override fun hashCode(): Int = name.hashCode()
        override fun toString(): String = name

        override val isWide: Boolean
            get() = this == LONG || this == DOUBLE

        override fun toBasicType(): Basic = this

        val isPrimitiveNumber: Boolean
            get() = this == INT || this == SHORT || this == LONG || this == FLOAT || this == DOUBLE

        override val isVoid: Boolean
            get() = name == "void"

        companion object {
            var BOOLEAN = Basic("boolean")
            var INT = Basic("int")
            var SHORT = Basic("short")
            var LONG = Basic("long")
            var FLOAT = Basic("float")
            var DOUBLE = Basic("double")
            var VOID = Basic("void")
        }
    }

    class Array(val elementType: JavaType) : JavaType() {

        override val nonGenericClassName: String
            get() = throw UnsupportedOperationException()

        override fun resolve(env: TypeEnvironment): JavaType = Array(elementType.resolve(env))

        override fun equals(other: Any?) = other is Array && elementType == other.elementType
        override fun hashCode() = elementType.hashCode()
        override fun toString() = "$elementType[]"
    }

    class InnerClass(val outer: JavaType, val name: String) : JavaType() {

        override val nonGenericClassName: String
            get() = outer.nonGenericClassName + '$' + name

        override fun resolve(env: TypeEnvironment): JavaType = InnerClass(outer.resolve(env), name)

        override fun equals(other: Any?) = other is InnerClass && outer == other.outer && name == other.name
        override fun hashCode() = Objects.hash(outer, name)
        override fun toString() = "$outer.$name"
    }

    /**
     * Represents a generic Java type, like [java.lang.reflect.ParameterizedType].
     */
    class Parameterized(val baseType: JavaType, val arguments: List<JavaType>) : JavaType() {

        init {
            if (arguments.isEmpty()) throw IllegalArgumentException("no arguments for generic type")
        }

        override val nonGenericClassName: String
            get() = baseType.nonGenericClassName

        override fun resolve(env: TypeEnvironment): JavaType =
                Parameterized(baseType.resolve(env), arguments.map { it.resolve(env) })

        override fun equals(other: Any?) = other is Parameterized && baseType == other.baseType && arguments == other.arguments
        override fun hashCode() = Objects.hash(baseType, arguments)
        override fun toString(): String = baseType.toString() + arguments.joinToString(", ", "<", ">")
    }

    class Variable(val name: String) : JavaType() {

        override val nonGenericClassName: String
            get() = throw UnsupportedOperationException()

        override fun resolve(env: TypeEnvironment): JavaType =
                env.lookup(this) ?: this

        override fun toString() = name
        override fun equals(other: Any?) = other is Variable && name == other.name
        override fun hashCode() = name.hashCode()
    }

    /**
     * Represents a wildcard type, like [java.lang.reflect.WildcardType].
     */
    class Wildcard(val upperBound: JavaType?, val lowerBound: JavaType?) : JavaType() {

        override val nonGenericClassName: String
            get() = throw UnsupportedOperationException()

        override fun resolve(env: TypeEnvironment): JavaType {
            return Wildcard(upperBound?.resolve(env), lowerBound?.resolve(env))
        }

        override fun toString(): String {
            val sb = StringBuilder("?")

            if (upperBound != null)
                sb.append(" extends ").append(upperBound)

            if (lowerBound != null)
                sb.append(" super ").append(lowerBound)

            return sb.toString()
        }

        override fun equals(other: Any?): Boolean = other is Wildcard && upperBound == other.upperBound && lowerBound == other.lowerBound
        override fun hashCode() = Objects.hash(upperBound, lowerBound)

        companion object {
            fun unbounded(): JavaType = Wildcard(null, null)
            fun extending(type: JavaType): JavaType = Wildcard(type, null)
            fun withSuper(type: JavaType): JavaType = Wildcard(null, type)
        }
    }
}
