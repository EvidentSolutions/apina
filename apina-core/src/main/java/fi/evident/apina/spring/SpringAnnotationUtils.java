package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaAnnotation;

import java.util.Optional;

final class SpringAnnotationUtils {
    static Optional<String> getRequestParamName(JavaAnnotation annotation) {
        return valueOrSecondaryAttribute(annotation, "name");
    }

    public static Optional<String> getRequestMappingPath(JavaAnnotation annotation) {
        return valueOrSecondaryAttribute(annotation, "path");
    }

    private static Optional<String> valueOrSecondaryAttribute(JavaAnnotation annotation, String secondaryName) {
        Optional<String> value = annotation.getAttribute("value", String.class);
        if (value.isPresent())
            return value;
        else
            return annotation.getAttribute(secondaryName, String.class);
    }
}
