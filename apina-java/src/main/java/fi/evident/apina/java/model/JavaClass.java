package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Contains all information read about class.
 */
public final class JavaClass implements JavaAnnotatedElement {

    private final JavaBasicType name;
    private final JavaBasicType superName;
    private final List<JavaBasicType> interfaces;
    private final List<JavaAnnotation> annotations = new ArrayList<>();
    private final List<JavaField> fields = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();

    public JavaClass(JavaBasicType name, JavaBasicType superName, List<JavaBasicType> interfaces) {
        this.name = requireNonNull(name);
        this.superName = requireNonNull(superName);
        this.interfaces = unmodifiableList(requireNonNull(interfaces));
    }

    public JavaBasicType getName() {
        return name;
    }

    public JavaBasicType getSuperName() {
        return superName;
    }

    public List<JavaBasicType> getInterfaces() {
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
        return name.toString();
    }
}
