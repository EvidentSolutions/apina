package fi.evident.apina.spring.java.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Contains metadata for all loaded classes.
 */
public final class ClassMetadataCollection {

    private final List<JavaClass> classes = new ArrayList<>();

    public ClassMetadataCollection(Collection<JavaClass> classes) {
        this.classes.addAll(classes);
    }

    public List<JavaClass> findClassesWithAnnotation(QualifiedName annotationType) {
        return classes.stream()
                .filter(c -> c.hasAnnotation(annotationType))
                .collect(toList());
    }
}
