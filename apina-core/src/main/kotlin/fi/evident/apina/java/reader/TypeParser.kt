package fi.evident.apina.java.reader

import fi.evident.apina.java.model.MethodSignature
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader

fun parseJavaType(typeDescriptor: String, signature: String?): JavaType =
    if (signature != null)
        parseGenericType(signature)
    else
        parseTypeDescriptor(typeDescriptor)

fun parseGenericType(signature: String): JavaType {
    val visitor = TypeBuildingSignatureVisitor()

    SignatureReader(signature).acceptType(visitor)

    return visitor.get()
}

fun parseTypeDescriptor(typeDescriptor: String) = javaType(Type.getType(typeDescriptor))

fun parseBasicTypeDescriptor(typeDescriptor: String): JavaType.Basic = parseTypeDescriptor(typeDescriptor).toBasicType()

fun parseObjectType(internalName: String) = javaType(Type.getObjectType(internalName))

fun parseMethodSignature(methodDescriptor: String, signature: String?): MethodSignature {
    val legacySignature = parseMethodDescriptor(methodDescriptor)
    if (signature != null) {
        val genericSignature = parseGenericMethodSignature(signature)

        // There are classes in the wild with differing argument counts for the two signatures.
        // At least guava-jdk5-13.0's com/google/common/collect/AbstractMultimap$WrappedSortedSet
        // is like this. All of the cases are the same: the implicit parent-class argument as the
        // first argument is left out. If we detect that, create a new argument list mostly based
        // on the generic signature, but using the parent type from non-generic descriptor.
        return if (legacySignature.argumentCount == genericSignature.argumentCount + 1) {
            val implicitParentType = legacySignature.argumentTypes[0]
            MethodSignature(
                genericSignature.returnType,
                listOf(implicitParentType) + genericSignature.argumentTypes,
                genericSignature.schema
            )
        } else {
            assert(legacySignature.argumentCount == genericSignature.argumentCount)
            genericSignature
        }
    }

    return legacySignature
}

fun parseGenericMethodSignature(signature: String): MethodSignature {
    val visitor = MethodSignatureVisitor()

    SignatureReader(signature).accept(visitor)

    return visitor.get()
}

fun parseMethodDescriptor(methodDescriptor: String): MethodSignature {
    val methodType = Type.getMethodType(methodDescriptor)

    val returnType = javaType(methodType.returnType)
    val argumentTypes = methodType.argumentTypes.map(::javaType)

    return MethodSignature(returnType, argumentTypes, TypeSchema())
}

fun javaType(type: Type): JavaType =
    if (type.sort == Type.ARRAY) {
        var javaType: JavaType = JavaType.Basic(type.elementType.className)

        repeat(type.dimensions) {
            javaType = JavaType.Array(javaType)
        }

        javaType

    } else {
        JavaType.Basic(type.className)
    }
