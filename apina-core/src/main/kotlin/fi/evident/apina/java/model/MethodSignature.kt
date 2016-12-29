package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema

/**
 * Contains parameter types and return types of a method.
 */
class MethodSignature(val returnType: JavaType, val argumentTypes: List<JavaType>, val schema: TypeSchema) {

    val argumentCount: Int
        get() = argumentTypes.size

    val parameters: List<JavaParameter>
        get() = argumentTypes.map(::JavaParameter)
}
