package fi.evident.apina.spring.java.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class JavaMethod implements JavaAnnotatedElement {

    private final String name;
    private final JavaVisibility visibility;
    private final List<AnnotationMetadata> annotations = new ArrayList<>();
    private final JavaType returnType;
    private final List<JavaType> argumentTypes;

    public JavaMethod(String name, JavaVisibility visibility, JavaType returnType, List<JavaType> argumentTypes) {
        this.name = requireNonNull(name);
        this.visibility = requireNonNull(visibility);
        this.returnType = requireNonNull(returnType);
        this.argumentTypes = unmodifiableList(requireNonNull(argumentTypes));
    }

    public String getName() {
        return name;
    }

    public JavaVisibility getVisibility() {
        return visibility;
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public List<JavaType> getArgumentTypes() {
        return argumentTypes;
    }

    @Override
    public List<AnnotationMetadata> getAnnotations() {
        return unmodifiableList(annotations);
    }

    public void addAnnotation(AnnotationMetadata annotation) {
        annotations.add(requireNonNull(annotation));
    }

    @Override
    public String toString() {
        return visibility + " " + returnType + " " + name + argumentTypes.stream().map(JavaType::toString).collect(joining(", ", "(", ")"));
    }
}
