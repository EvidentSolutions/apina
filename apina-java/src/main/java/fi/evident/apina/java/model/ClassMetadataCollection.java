package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;

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

    public List<JavaClass> findClassesWithAnnotation(JavaBasicType annotationType) {
        return classes.stream()
                .filter(c -> c.hasAnnotation(annotationType))
                .collect(toList());
    }
}
