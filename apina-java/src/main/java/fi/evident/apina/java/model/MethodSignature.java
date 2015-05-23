package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Contains parameter types and return types of a method.
 */
public final class MethodSignature {

    private final JavaType returnType;
    private final List<JavaType> argumentTypes;
    private final TypeSchema schema;

    public MethodSignature(JavaType returnType, List<JavaType> argumentTypes, TypeSchema schema) {
        this.returnType = requireNonNull(returnType);
        this.argumentTypes = unmodifiableList(requireNonNull(argumentTypes));
        this.schema = requireNonNull(schema);
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public List<JavaType> getArgumentTypes() {
        return argumentTypes;
    }

    public List<JavaParameter> getParameters() {
        return argumentTypes.stream().map(JavaParameter::new).collect(toList());
    }

    public TypeSchema getSchema() {
        return schema;
    }
}
