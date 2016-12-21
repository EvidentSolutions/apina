package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaAnnotatedElement;
import fi.evident.apina.java.model.JavaAnnotation;

import java.util.List;

import static java.util.Arrays.asList;

final class MockAnnotatedElement implements JavaAnnotatedElement {

    private final List<JavaAnnotation> annotations;

    MockAnnotatedElement(JavaAnnotation... annotations) {
        this.annotations = asList(annotations);
    }

    @Override
    public List<JavaAnnotation> getAnnotations() {
        return annotations;
    }
}
