package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Parameter definition for a {@link JavaMethod}.
 */
public final class JavaParameter implements JavaAnnotatedElement {

    private Optional<String> name = Optional.empty();

    private final JavaType type;

    private final List<JavaAnnotation> annotations = new ArrayList<>();

    public JavaParameter(JavaType type) {
        this.type = requireNonNull(type);
    }

    public Optional<String> getName() {
        return name;
    }

    public JavaType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " "  + name.orElse("<unknown>");
    }

    public void initName(String name) {
        if (this.name.isPresent())
            throw new IllegalStateException("name for parameter " + this.name.get() + " has already been initialized");

        this.name = Optional.of(name);
    }

    @Override
    public List<JavaAnnotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(JavaAnnotation annotation) {
        this.annotations.add(requireNonNull(annotation));
    }
}
