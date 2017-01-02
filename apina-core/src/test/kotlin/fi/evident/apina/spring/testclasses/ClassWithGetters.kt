package fi.evident.apina.spring.testclasses

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "unused")
class ClassWithGetters {

    val int: Int
        get() = throw UnsupportedOperationException()

    val integer: Integer
        get() = throw UnsupportedOperationException()

    val string: String
        get() = throw UnsupportedOperationException()

    val isBoolean: Boolean
        get() = throw UnsupportedOperationException()

    val booleanNonPrimitive: java.lang.Boolean
        get() = throw UnsupportedOperationException()
}
