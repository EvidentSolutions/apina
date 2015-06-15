package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.JavaArrayType;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import java.util.List;

import static fi.evident.apina.utils.CollectionUtils.cons;
import static fi.evident.apina.utils.CollectionUtils.map;

/**
 * Parses Java's internal type names, descriptors and signatures into
 */
final class TypeParser {

    public static JavaType parseJavaType(@NotNull String typeDescriptor, @Nullable String signature) {
        if (signature != null)
            return parseGenericType(signature);
        else
            return parseTypeDescriptor(typeDescriptor);
    }

    public static JavaType parseGenericType(String signature) {
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor();

        new SignatureReader(signature).acceptType(visitor);

        return visitor.get();
    }

    public static JavaType parseTypeDescriptor(String typeDescriptor) {
        return javaType(Type.getType(typeDescriptor));
    }

    public static JavaBasicType parseBasicTypeDescriptor(String typeDescriptor) {
        return parseTypeDescriptor(typeDescriptor).toBasicType();
    }

    public static JavaType parseObjectType(String internalName) {
        return javaType(Type.getObjectType(internalName));
    }

    public static MethodSignature parseMethodSignature(String methodDescriptor, @Nullable String signature) {
        MethodSignature legacySignature = parseMethodDescriptor(methodDescriptor);
        if (signature != null) {
            MethodSignature genericSignature = parseGenericMethodSignature(signature);

            // There are classes in the wild with differing argument counts for the two signatures.
            // At least guava-jdk5-13.0's com/google/common/collect/AbstractMultimap$WrappedSortedSet
            // is like this. All of the cases are the same: the implicit parent-class argument as the
            // first argument is left out. If we detect that, create a new argument list mostly based
            // on the generic signature, but using the parent type from non-generic descriptor.
            if (legacySignature.getArgumentCount() == genericSignature.getArgumentCount() + 1) {
                JavaType implicitParentType = legacySignature.getArgumentTypes().get(0);
                return new MethodSignature(
                        genericSignature.getReturnType(),
                        cons(implicitParentType, genericSignature.getArgumentTypes()),
                        genericSignature.getSchema());
            } else {
                assert legacySignature.getArgumentCount() == genericSignature.getArgumentCount();
                return genericSignature;
            }
        }

        return legacySignature;
    }

    static MethodSignature parseGenericMethodSignature(String signature) {
        MethodSignatureVisitor visitor = new MethodSignatureVisitor();

        new SignatureReader(signature).accept(visitor);

        return visitor.get();
    }

    @NotNull
    static MethodSignature parseMethodDescriptor(String methodDescriptor) {
        Type methodType = Type.getMethodType(methodDescriptor);

        JavaType returnType = javaType(methodType.getReturnType());
        List<JavaType> argumentTypes = map(methodType.getArgumentTypes(), TypeParser::javaType);

        return new MethodSignature(returnType, argumentTypes, new TypeSchema());
    }

    private static JavaType javaType(Type type) {
        if (type.getSort() == Type.ARRAY) {
            JavaType javaType = new JavaBasicType(type.getElementType().getClassName());

            for (int i = 0, dimensions = type.getDimensions(); i < dimensions; i++)
                javaType = new JavaArrayType(javaType);

            return javaType;

        } else {
            return new JavaBasicType(type.getClassName());
        }
    }
}
