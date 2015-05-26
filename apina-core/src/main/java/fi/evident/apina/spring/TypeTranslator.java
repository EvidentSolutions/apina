package fi.evident.apina.spring;

import fi.evident.apina.java.model.ClassMetadataCollection;
import fi.evident.apina.java.model.JavaClass;
import fi.evident.apina.java.model.JavaMethod;
import fi.evident.apina.java.model.type.*;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.ClassDefinition;
import fi.evident.apina.model.PropertyDefinition;
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
    private final ApiDefinition api;

    public TypeTranslator(ClassMetadataCollection classes, TypeSchema schema, ApiDefinition api) {
        this.classes = requireNonNull(classes);
        this.schema = requireNonNull(schema);
        this.api = requireNonNull(api);
    }

    public ApiType translateType(JavaType javaType) {
        return javaType.accept(new JavaTypeVisitor<TypeSchema, ApiType>() {
            @Override
            public ApiType visit(JavaArrayType type, TypeSchema ctx) {
                return new ApiArrayType(translateType(type.getElementType()));
            }

            @Override
            public ApiType visit(JavaBasicType type, TypeSchema ctx) {
                if (classes.isInstanceOf(type, Collection.class)) {
                    return new ApiArrayType(ApiPrimitiveType.UNKNOWN);

                } else if (classes.isInstanceOf(type, Map.class)) {
                    return ApiPrimitiveType.DICTIONARY;

                } else if (type.equals(new JavaBasicType(String.class))) {
                    return ApiPrimitiveType.STRING;

                } else if (classes.isNumber(type)) {
                    return ApiPrimitiveType.NUMBER;

                } else if (type.equals(JavaBasicType.BOOLEAN) || type.equals(new JavaBasicType(Boolean.class))) {
                    return ApiPrimitiveType.NUMBER;

                } else if (type.equals(new JavaBasicType(Object.class))) {
                    return ApiPrimitiveType.UNKNOWN;

                } else if (type.isVoid()) {
                    return ApiPrimitiveType.VOID;

                } else {
                    return translateClassType(type);
                }
            }

            @Override
            public ApiType visit(JavaParameterizedType type, TypeSchema ctx) {
                JavaType baseType = type.getBaseType();
                List<JavaType> arguments = type.getArguments();

                if (classes.isInstanceOf(baseType, Collection.class) && arguments.size() == 1)
                    return new ApiArrayType(translateType(arguments.get(0)));
                else
                    return translateType(baseType);
            }

            @Override
            public ApiType visit(JavaTypeVariable type, TypeSchema ctx) {
                List<JavaType> bounds = ctx.getTypeBounds(type);

                // TODO: merge the bounds instead of picking the first one
                if (!bounds.isEmpty())
                    return translateType(bounds.get(0));
                else
                    return ApiPrimitiveType.UNKNOWN;
            }

            @Override
            public ApiType visit(JavaWildcardType type, TypeSchema ctx) {
                return type.getLowerBound().map(TypeTranslator.this::translateType).orElse(ApiPrimitiveType.UNKNOWN);
            }
        }, schema);
    }

    private ApiType translateClassType(JavaBasicType type) {
        ApiClassType classType = new ApiClassType(translateName(type.getName()));

        if (!api.containsClassType(classType)) {
            JavaClass aClass = classes.findClass(type).orElse(null);
            if (aClass != null) {
                ClassDefinition classDefinition = new ClassDefinition(classType);

                // We must first add the definition to api and only then proceed to
                // initialize it because initialization of properties could refer
                // back to this same class and we'd get infinite recursion if the
                // class is not already installed.
                api.addClassDefinition(classDefinition);
                initClassDefinition(classDefinition, aClass);
            }
        }

        return classType;
    }

    private void initClassDefinition(ClassDefinition classDefinition, JavaClass javaClass) {
        // TODO: support Jackson's annotations to override default mappings

        javaClass.getPublicFields()
            .filter(f -> !f.isStatic())
            .forEach(field -> {
                String name = field.getName();
                ApiType type = translateType(field.getType());

                classDefinition.addProperty(new PropertyDefinition(name, type));
            });

        javaClass.getPublicMethods()
                .filter(JavaMethod::isGetter)
                .forEach(method -> {
                    String name = uncapitalize(method.getName().substring(3));
                    ApiType type = translateType(method.getReturnType());

                    classDefinition.addProperty(new PropertyDefinition(name, type));
                });
    }

    static String translateName(String qualifiedName) {
        // TODO: smarter translation
        int lastDot = max(qualifiedName.lastIndexOf('.'), qualifiedName.lastIndexOf('$'));
        if (lastDot != -1)
            return qualifiedName.substring(lastDot + 1);
        else
            return qualifiedName;
    }

    private static String uncapitalize(String s) {
        if (!s.isEmpty() && Character.isUpperCase(s.charAt(0)))
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        else
            return s;
    }
}
