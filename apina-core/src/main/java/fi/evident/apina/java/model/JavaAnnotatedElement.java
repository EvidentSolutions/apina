package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;

import java.util.List;
import java.util.Optional;

public interface JavaAnnotatedElement {

    List<JavaAnnotation> getAnnotations();

    default boolean hasAnnotation(JavaBasicType annotationType) {
        return findAnnotation(annotationType).isPresent();
    }

    default Optional<JavaAnnotation> findAnnotation(JavaBasicType annotationType) {
        return getAnnotations().stream()
                .filter(a -> annotationType.equals(a.getName()))
                .findFirst();
    }

    default JavaAnnotation getAnnotation(JavaBasicType annotationType) {
        return findAnnotation(annotationType).orElseThrow(() -> new IllegalArgumentException("annotation not present : " + annotationType));
    }

    default <T> Optional<T> findUniqueAnnotationAttributeValue(JavaBasicType annotationType, String attributeName, Class<T> type) {
        return findAnnotation(annotationType).flatMap(a -> a.findUniqueAttributeValue(attributeName, type));
    }

}