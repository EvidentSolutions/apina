package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.JavaArrayType;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaTypeVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Parses Java's internal type names, descriptors and signatures into
 */
final class TypeParser {

    public static JavaType parseJavaType(@NotNull String typeDescriptor, @Nullable String signature, Map<String, JavaTypeVariable> typeVariableMap) {
        if (signature != null)
            return parseGenericType(signature, typeVariableMap);
        else
            return parseTypeDescriptor(typeDescriptor);
    }

    public static JavaType parseGenericType(String signature, Map<String, JavaTypeVariable> typeVariableMap) {
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor(typeVariableMap);

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

    public static MethodSignature parseMethodSignature(String methodDescriptor, @Nullable String signature, Map<String, JavaTypeVariable> typeVariableMap) {
        if (signature != null)
            return parseGenericMethodSignature(signature, typeVariableMap);
        else
            return parseMethodDescriptor(methodDescriptor);
    }

    static MethodSignature parseGenericMethodSignature(String signature, Map<String, JavaTypeVariable> typeVariableMap) {
        MethodSignatureVisitor visitor = new MethodSignatureVisitor(typeVariableMap);

        new SignatureReader(signature).accept(visitor);

        return visitor.get();
    }

    @NotNull
    static MethodSignature parseMethodDescriptor(String methodDescriptor) {
        Type methodType = Type.getMethodType(methodDescriptor);

        JavaType returnType = javaType(methodType.getReturnType());
        List<JavaType> argumentTypes = Stream.of(methodType.getArgumentTypes())
                .map(TypeParser::javaType)
                .collect(toList());

        return new MethodSignature(returnType, argumentTypes);
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
