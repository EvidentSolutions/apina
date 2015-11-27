package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaAnnotation;

import java.util.Optional;

final class SpringAnnotationUtils {
    static Optional<String> getRequestParamName(JavaAnnotation annotation) {
        Optional<String> value = annotation.getAttribute("value", String.class);
        if (value.isPresent())
            return value;
        else
            return annotation.getAttribute("name", String.class);
    }
}
