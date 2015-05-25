package fi.evident.apina.spring;

import fi.evident.apina.java.model.ClassMetadataCollection;
import fi.evident.apina.java.model.type.*;
import fi.evident.apina.model.type.ApiArrayType;
import fi.evident.apina.model.type.ApiClassType;
import fi.evident.apina.model.type.ApiPrimitiveType;
import fi.evident.apina.model.type.ApiType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

/**
 * Translates Java types to model types.
 */
final class TypeTranslator {

    private final ClassMetadataCollection classes;
    private final TypeSchema schema;

    public TypeTranslator(ClassMetadataCollection classes, TypeSchema schema) {
        this.classes = requireNonNull(classes);
        this.schema = requireNonNull(schema);
    }

    public ApiType resolveDataType(JavaType javaType) {
        return javaType.accept(new JavaTypeVisitor<TypeSchema, ApiType>() {
            @Override
            public ApiType visit(JavaArrayType type, TypeSchema ctx) {
                return new ApiArrayType(resolveDataType(type.getElementType()));
            }

            @Override
            public ApiType visit(JavaBasicType type, TypeSchema ctx) {
                if (classes.isInstanceOf(type, Collection.class)) {
                    return new ApiArrayType(ApiPrimitiveType.UNKNOWN);

                } else if (classes.isInstanceOf(type, Map.class)) {
                    return ApiPrimitiveType.DICTIONARY;

                } else if (type.equals(new JavaBasicType(String.class))) {
                    return ApiPrimitiveType.STRING;

                } else if (type.equals(new JavaBasicType(Integer.class)) || type.equals(new JavaBasicType(int.class))) {
                    return ApiPrimitiveType.INTEGER;

                } else if (type.equals(new JavaBasicType(Object.class))) {
                    return ApiPrimitiveType.UNKNOWN;

                } else if (type.isVoid()) {
                    return ApiPrimitiveType.VOID;

                } else {
                    return new ApiClassType(translateName(type.getName()));
                }
            }

            @Override
            public ApiType visit(JavaParameterizedType type, TypeSchema ctx) {
                JavaType baseType = type.getBaseType();
                List<JavaType> arguments = type.getArguments();

                if (classes.isInstanceOf(baseType, Collection.class) && arguments.size() == 1)
                    return new ApiArrayType(resolveDataType(arguments.get(0)));
                else
                    return resolveDataType(baseType);
            }

            @Override
            public ApiType visit(JavaTypeVariable type, TypeSchema ctx) {
                List<JavaType> bounds = ctx.getTypeBounds(type);

                // TODO: merge the bounds instead of picking the first one
                if (!bounds.isEmpty())
                    return resolveDataType(bounds.get(0));
                else
                    return ApiPrimitiveType.UNKNOWN;
            }

            @Override
            public ApiType visit(JavaWildcardType type, TypeSchema ctx) {
                return type.getLowerBound().map(TypeTranslator.this::resolveDataType).orElse(ApiPrimitiveType.UNKNOWN);
            }
        }, schema);
    }

    static String translateName(String qualifiedName) {
        // TODO: smarter translation
        int lastDot = max(qualifiedName.lastIndexOf('.'), qualifiedName.lastIndexOf('$'));
        if (lastDot != -1)
            return qualifiedName.substring(lastDot + 1);
        else
            return qualifiedName;
    }
}
