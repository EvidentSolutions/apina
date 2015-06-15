package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;

import java.util.List;

import static fi.evident.apina.utils.CollectionUtils.map;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

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

    public int getArgumentCount() {
        return argumentTypes.size();
    }

    public List<JavaType> getArgumentTypes() {
        return argumentTypes;
    }

    public List<JavaParameter> getParameters() {
        return map(argumentTypes, JavaParameter::new);
    }

    public TypeSchema getSchema() {
        return schema;
    }
}
