package fi.evident.apina.spring.java.reader;

import fi.evident.apina.spring.java.model.JavaType;
import fi.evident.apina.spring.java.model.MethodSignature;
import fi.evident.apina.spring.java.model.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

final class TypeParser {
    public static JavaType parseJavaType(@NotNull String typeDescriptor, @Nullable String signature) {
        // TODO: use signature if available
        return javaType(Type.getType(typeDescriptor));
    }

    public static QualifiedName parseTypeDescriptor(String typeDescriptor) {
        return new QualifiedName(Type.getType(typeDescriptor).getClassName());
    }

    public static QualifiedName parseObjectType(String internalName) {
        return new QualifiedName(Type.getObjectType(internalName).getClassName());
    }

    public static MethodSignature parseMethodSignature(String methodDescriptor, @Nullable String signature) {
        // TODO: use signature if available
        Type methodType = Type.getMethodType(methodDescriptor);

        return new MethodSignature(javaType(methodType.getReturnType()), Stream.of(methodType.getArgumentTypes()).map(TypeParser::javaType).collect(toList()));
    }

    private static JavaType javaType(Type type) {
        return new JavaType(new QualifiedName(type.getClassName()));
    }
}
