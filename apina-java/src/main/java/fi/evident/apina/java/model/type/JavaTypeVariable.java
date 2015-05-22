package fi.evident.apina.java.model.type;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public final class JavaTypeVariable extends JavaType {

    private final String name;
    private final List<JavaType> bounds;

    public JavaTypeVariable(String name) {
        this(name, emptyList());
    }

    public JavaTypeVariable(String name, List<JavaType> bounds) {
        this.name = requireNonNull(name);
        this.bounds = unmodifiableList(requireNonNull(bounds));
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (bounds.isEmpty())
            return name;
        else
            return name + " extends " + bounds.stream().map(JavaType::toString).collect(joining(" & "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaTypeVariable that = (JavaTypeVariable) o;

        return name.equals(that.name)
            && bounds.equals(that.bounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bounds);
    }
}
