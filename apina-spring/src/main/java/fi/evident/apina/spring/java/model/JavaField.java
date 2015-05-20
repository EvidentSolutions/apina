package fi.evident.apina.spring.java.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class JavaField implements JavaAnnotatedElement {

    private final String name;
    private final JavaVisibility visibility;
    private final JavaType type;
    private final List<AnnotationMetadata> annotations = new ArrayList<>();

    public JavaField(String name, JavaVisibility visibility, JavaType type) {
        this.name = requireNonNull(name);
        this.visibility = requireNonNull(visibility);
        this.type = requireNonNull(type);
    }

    public String getName() {
        return name;
    }

    public JavaVisibility getVisibility() {
        return visibility;
    }

    public JavaType getType() {
        return type;
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
        return visibility + " " + type + " " + name;
    }
}
