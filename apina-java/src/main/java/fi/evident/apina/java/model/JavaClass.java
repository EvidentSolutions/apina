package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Contains all information read about class.
 */
public final class JavaClass implements JavaAnnotatedElement {

    private final String name;
    private final JavaBasicType superName;
    private final List<JavaType> interfaces;
    private final List<JavaAnnotation> annotations = new ArrayList<>();
    private final List<JavaField> fields = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();

    public JavaClass(String name, JavaBasicType superName, List<JavaType> interfaces) {
        this.name = requireNonNull(name);
        this.superName = requireNonNull(superName);
        this.interfaces = unmodifiableList(requireNonNull(interfaces));
    }

    public String getName() {
        return name;
    }

    public JavaType getSuperName() {
        return superName;
    }

    public List<JavaType> getInterfaces() {
        return unmodifiableList(interfaces);
    }

    public List<JavaField> getFields() {
        return unmodifiableList(fields);
    }

    public List<JavaMethod> getMethods() {
        return unmodifiableList(methods);
    }

    @Override
    public List<JavaAnnotation> getAnnotations() {
        return unmodifiableList(annotations);
    }

    public void addAnnotation(JavaAnnotation annotation) {
        annotations.add(requireNonNull(annotation));
    }

    public void addField(JavaField field) {
        fields.add(requireNonNull(field));
    }

    public void addMethod(JavaMethod method) {
        methods.add(requireNonNull(method));
    }

    @Override
    public String toString() {
        return name;
    }

    public JavaField getField(String name) {
        JavaField field = fields.stream().filter(f -> name.equals(f.getName())).findFirst().orElse(null);
        if (field != null)
            return field;
        else
            throw new RuntimeException("field not found " + name);
    }
}
