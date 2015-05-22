package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
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

    public static JavaBasicType parseTypeDescriptor(String typeDescriptor) {
        return new JavaBasicType(Type.getType(typeDescriptor).getClassName());
    }

    public static JavaBasicType parseObjectType(String internalName) {
        return new JavaBasicType(Type.getObjectType(internalName).getClassName());
    }

    public static MethodSignature parseMethodSignature(String methodDescriptor, @Nullable String signature) {
        // TODO: use signature if available
        Type methodType = Type.getMethodType(methodDescriptor);

        return new MethodSignature(javaType(methodType.getReturnType()), Stream.of(methodType.getArgumentTypes()).map(TypeParser::javaType).collect(toList()));
    }

    private static JavaType javaType(Type type) {
        return new JavaBasicType(type.getClassName());
    }
}
