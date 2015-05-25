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
        if (signature != null)
            return parseGenericMethodSignature(signature);
        else
            return parseMethodDescriptor(methodDescriptor);
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
