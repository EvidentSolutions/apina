package fi.evident.apina.spring.java.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Contains all information read about class.
 */
public final class JavaClass implements JavaAnnotatedElement {

    private final QualifiedName name;
    private final QualifiedName superName;
    private final List<QualifiedName> interfaces;
    private final List<AnnotationMetadata> annotations = new ArrayList<>();
    private final List<JavaField> fields = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();

    public JavaClass(QualifiedName name, QualifiedName superName, List<QualifiedName> interfaces) {
        this.name = requireNonNull(name);
        this.superName = requireNonNull(superName);
        this.interfaces = unmodifiableList(requireNonNull(interfaces));
    }

    public QualifiedName getName() {
        return name;
    }

    public QualifiedName getSuperName() {
        return superName;
    }

    public List<QualifiedName> getInterfaces() {
        return unmodifiableList(interfaces);
    }

    public List<JavaField> getFields() {
        return unmodifiableList(fields);
    }

    public List<JavaMethod> getMethods() {
        return unmodifiableList(methods);
    }

    @Override
    public List<AnnotationMetadata> getAnnotations() {
        return unmodifiableList(annotations);
    }

    public void addAnnotation(AnnotationMetadata annotation) {
        annotations.add(requireNonNull(annotation));
    }

    public void addField(JavaField field) {
        fields.add(requireNonNull(field));
    }

    public void addMethod(JavaMethod method) {
        methods.add(requireNonNull(method));
    }
}
