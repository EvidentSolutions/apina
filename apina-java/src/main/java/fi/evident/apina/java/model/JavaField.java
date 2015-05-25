package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class JavaField implements JavaAnnotatedElement {

    private final String name;
    private final JavaVisibility visibility;
    private final JavaType type;
    private final int modifiers;
    private final List<JavaAnnotation> annotations = new ArrayList<>();

    public JavaField(String name, JavaVisibility visibility, JavaType type, int modifiers) {
        this.name = requireNonNull(name);
        this.visibility = requireNonNull(visibility);
        this.type = requireNonNull(type);
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public JavaVisibility getVisibility() {
        return visibility;
    }

    public boolean isPublic() {
        return visibility == JavaVisibility.PUBLIC;
    }

    public JavaType getType() {
        return type;
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
        StringBuilder sb = new StringBuilder();

        if (visibility != JavaVisibility.PACKAGE)
            sb.append(visibility).append(' ');

        if (isStatic())
            sb.append("static ");

        sb.append(type).append(' ').append(name);

        return sb.toString();
    }
}
