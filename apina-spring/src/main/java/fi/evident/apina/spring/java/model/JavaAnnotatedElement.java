package fi.evident.apina.spring.java.model;

import java.util.List;

public interface JavaAnnotatedElement {

    List<AnnotationMetadata> getAnnotations();

    default boolean hasAnnotation(QualifiedName annotationType) {
        return getAnnotations().stream()
                .anyMatch(a -> annotationType.equals(a.getName()));
    }
}
