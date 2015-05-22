package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class JavaMethod implements JavaAnnotatedElement {

    private final String name;
    private final JavaVisibility visibility;
    private final List<JavaAnnotation> annotations = new ArrayList<>();
    private final JavaType returnType;
    private final List<JavaType> argumentTypes;
    private final int modifiers;

    public JavaMethod(String name, JavaVisibility visibility, JavaType returnType, List<JavaType> argumentTypes, int modifiers) {
        this.name = requireNonNull(name);
        this.visibility = requireNonNull(visibility);
        this.returnType = requireNonNull(returnType);
        this.argumentTypes = unmodifiableList(requireNonNull(argumentTypes));
        this.modifiers = modifiers;
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

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    @Override
    public List<JavaAnnotation> getAnnotations() {
        return unmodifiableList(annotations);
    }

    public void addAnnotation(JavaAnnotation annotation) {
        annotations.add(requireNonNull(annotation));
    }

    @Override
    public String toString() {
        return visibility + " " + returnType + " " + name + argumentTypes.stream().map(JavaType::toString).collect(joining(", ", "(", ")"));
    }
}
