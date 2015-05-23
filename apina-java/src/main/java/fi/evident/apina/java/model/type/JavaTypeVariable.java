package fi.evident.apina.java.model.type;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class JavaTypeVariable extends JavaType {

    private final String name;
    private final List<JavaType> bounds = new ArrayList<>();

    public JavaTypeVariable(String name) {
        this.name = requireNonNull(name);
    }

    public List<JavaType> getBounds() {
        return unmodifiableList(bounds);
    }

    public void addBound(JavaType bound) {
        bounds.add(requireNonNull(bound));
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
