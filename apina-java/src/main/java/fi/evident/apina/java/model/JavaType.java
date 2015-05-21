package fi.evident.apina.java.model;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class JavaType {

    public final QualifiedName qualifiedName;

    public final List<JavaType> arguments;

    public JavaType(QualifiedName qualifiedName) {
        this(qualifiedName, emptyList());
    }

    public JavaType(QualifiedName qualifiedName, List<JavaType> arguments) {
        this.qualifiedName = requireNonNull(qualifiedName);
        this.arguments = unmodifiableList(requireNonNull(arguments));
    }

    public QualifiedName getQualifiedName() {
        return qualifiedName;
    }

    public List<JavaType> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        if (arguments.isEmpty())
            return qualifiedName.toString();

        return qualifiedName + arguments.stream().map(JavaType::toString).collect(joining(", ", "<", ">"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaType javaType = (JavaType) o;

        return qualifiedName.equals(javaType.qualifiedName)
                && arguments.equals(javaType.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName, arguments);
    }
}
