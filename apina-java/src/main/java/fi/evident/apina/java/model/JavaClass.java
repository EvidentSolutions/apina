package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Contains all information read about class.
 */
public final class JavaClass implements JavaAnnotatedElement {

    private final JavaType type;
    private final JavaType superClass;
    private final List<JavaType> interfaces;
    private final List<JavaAnnotation> annotations = new ArrayList<>();
    private final List<JavaField> fields = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final int modifiers;
    private final TypeSchema schema;

    public JavaClass(JavaType type, JavaType superClass, List<JavaType> interfaces, int modifiers, TypeSchema schema) {
        this.modifiers = modifiers;
        this.type = requireNonNull(type);
        this.superClass = requireNonNull(superClass);
        this.interfaces = unmodifiableList(requireNonNull(interfaces));
        this.schema = requireNonNull(schema);
    }

    public boolean isEnum() {
        return (modifiers & Opcodes.ACC_ENUM) != 0;
    }

    public String getName() {
        return type.toString();
    }

    public JavaType getType() {
        return type;
    }

    public JavaType getSuperClass() {
        return superClass;
    }

    public List<JavaType> getInterfaces() {
        return unmodifiableList(interfaces);
    }

    public List<JavaField> getFields() {
        return unmodifiableList(fields);
    }

    public Stream<JavaField> getPublicFields() {
        return fields.stream().filter(JavaField::isPublic);
    }

    public List<JavaMethod> getMethods() {
        return unmodifiableList(methods);
    }

    public Stream<JavaMethod> getPublicMethods() {
        return methods.stream().filter(JavaMethod::isPublic);
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
        return type.toString();
    }

    public JavaField getField(String name) {
        JavaField field = fields.stream().filter(f -> name.equals(f.getName())).findFirst().orElse(null);
        if (field != null)
            return field;
        else
            throw new RuntimeException("field not found " + name);
    }

    public TypeSchema getSchema() {
        return schema;
    }
}
