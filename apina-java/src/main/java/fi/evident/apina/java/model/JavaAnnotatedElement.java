package fi.evident.apina.java.model;

import java.util.List;
import java.util.Optional;

public interface JavaAnnotatedElement {

    List<JavaAnnotation> getAnnotations();

    default boolean hasAnnotation(QualifiedName annotationType) {
        return findAnnotation(annotationType).isPresent();
    }

    default Optional<JavaAnnotation> findAnnotation(QualifiedName annotationType) {
        return getAnnotations().stream()
                .filter(a -> annotationType.equals(a.getName()))
                .findFirst();
    }
}
