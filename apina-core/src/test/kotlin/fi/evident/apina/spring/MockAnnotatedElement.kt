package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotatedElement
import fi.evident.apina.java.model.JavaAnnotation

internal class MockAnnotatedElement(vararg annotations: JavaAnnotation) : JavaAnnotatedElement {
    override val annotations = annotations.asList()
}
