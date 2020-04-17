package fi.evident.apina.java.model.type

import kotlin.reflect.KClass

/**
 * Base class for all Java types, like [java.lang.reflect.Type].
 */
sealed class JavaType {

    open fun toBasicType(): Basic =
        throw ClassCastException("can't cast ${javaClass.name} to JavaType.Basic")

    abstract val nonGenericClassName: String

    val packageName: String
        get() = nonGenericClassName.substringBeforeLast('.', "")

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
    data class Basic(val name: String) : JavaType() {

        constructor(cl: Class<*>) : this(cl.name)

        constructor(cl: KClass<*>) : this(cl.java)

        override val nonGenericClassName: String
            get() = name

        override fun resolve(env: TypeEnvironment): JavaType = this

        override fun toString(): String = name

        override val isWide: Boolean
            get() = this == LONG || this == DOUBLE

        override fun toBasicType(): Basic = this

        val isPrimitiveNumber: Boolean
            get() = isPrimitiveIntegral || isPrimitiveFloatingPoint

        val isPrimitiveIntegral: Boolean
            get() = this == INT || this == SHORT || this == LONG

        private val isPrimitiveFloatingPoint: Boolean
            get() = this == FLOAT || this == DOUBLE

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

    data class Array(val elementType: JavaType) : JavaType() {

        override val nonGenericClassName: String
            get() = throw UnsupportedOperationException()

        override fun resolve(env: TypeEnvironment): JavaType = Array(elementType.resolve(env))

        override fun toString() = "$elementType[]"
    }

    data class InnerClass(private val outer: JavaType, val name: String) : JavaType() {

        override val nonGenericClassName: String
            get() = outer.nonGenericClassName + '$' + name

        override fun resolve(env: TypeEnvironment): JavaType = InnerClass(outer.resolve(env), name)

        override fun toString() = "$outer.$name"
    }

    /**
     * Represents a generic Java type, like [java.lang.reflect.ParameterizedType].
     */
    data class Parameterized(val baseType: JavaType, val arguments: List<JavaType>) : JavaType() {

        init {
            if (arguments.isEmpty()) throw IllegalArgumentException("no arguments for generic type")
        }

        override val nonGenericClassName: String
            get() = baseType.nonGenericClassName

        override fun resolve(env: TypeEnvironment): JavaType =
            Parameterized(baseType.resolve(env), arguments.map { it.resolve(env) })

        override fun toString(): String = baseType.toString() + arguments.joinToString(", ", "<", ">")
    }

    data class Variable(val name: String) : JavaType() {

        override val nonGenericClassName: String
            get() = throw UnsupportedOperationException()

        override fun resolve(env: TypeEnvironment): JavaType =
            env.lookup(this) ?: this

        override fun toString() = name
    }

    /**
     * Represents a wildcard type, like [java.lang.reflect.WildcardType].
     */
    data class Wildcard(val upperBound: JavaType?, val lowerBound: JavaType?) : JavaType() {

        override val nonGenericClassName: String
            get() = throw UnsupportedOperationException()

        override fun resolve(env: TypeEnvironment): JavaType {
            return Wildcard(upperBound?.resolve(env), lowerBound?.resolve(env))
        }

        override fun toString(): String = buildString {
            if (upperBound != null)
                append(" extends ").append(upperBound)

            if (lowerBound != null)
                append(" super ").append(lowerBound)
        }

        companion object {
            fun unbounded(): JavaType = Wildcard(null, null)
            fun extending(type: JavaType): JavaType = Wildcard(type, null)
            fun withSuper(type: JavaType): JavaType = Wildcard(null, type)
        }
    }
}
