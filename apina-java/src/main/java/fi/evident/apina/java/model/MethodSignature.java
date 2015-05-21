package fi.evident.apina.java.model;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Contains parameter types and return types of a method.
 */
public final class MethodSignature {

    private final JavaType returnType;
    private final List<JavaType> argumentTypes;

    public MethodSignature(JavaType returnType, List<JavaType> argumentTypes) {
        this.returnType = requireNonNull(returnType);
        this.argumentTypes = unmodifiableList(requireNonNull(argumentTypes));
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public List<JavaType> getArgumentTypes() {
        return argumentTypes;
    }
}
