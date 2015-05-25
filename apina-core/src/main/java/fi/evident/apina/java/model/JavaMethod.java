package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.apina.utils.CollectionUtils.join;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class JavaMethod implements JavaAnnotatedElement {

    private final JavaClass owningClass;
    private final String name;
    private final JavaVisibility visibility;
    private final List<JavaAnnotation> annotations = new ArrayList<>();
    private final JavaType returnType;
    private final List<JavaParameter> parameters;
    private final int modifiers;
    private final TypeSchema schema;

    public JavaMethod(JavaClass owningClass, String name, JavaVisibility visibility, JavaType returnType, List<JavaParameter> parameters, int modifiers, TypeSchema schema) {
        this.owningClass = requireNonNull(owningClass);
        this.name = requireNonNull(name);
        this.visibility = requireNonNull(visibility);
        this.returnType = requireNonNull(returnType);
        this.parameters = unmodifiableList(requireNonNull(parameters));
        this.modifiers = modifiers;
        this.schema = requireNonNull(schema);
    }

    public JavaClass getOwningClass() {
        return owningClass;
    }

    public String getName() {
        return name;
    }

    public boolean isPublic() {
        return visibility == JavaVisibility.PUBLIC;
    }

    public boolean isGetter() {
        return !isStatic() && parameters.isEmpty() && name.startsWith("get");
    }

    public JavaVisibility getVisibility() {
        return visibility;
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public TypeSchema getSchema() {
        return schema;
    }

    public TypeSchema getEffectiveSchema() {
        return schema.mergeWithParent(owningClass.getSchema());
    }

    public List<JavaParameter> getParameters() {
        return parameters;
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
        return visibility + " " + returnType + " " + name + join(parameters, ",", "(", ")");
    }
}
