package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType

class JavaRecordComponent(val name: String,
                          val type: JavaType) : JavaAnnotatedElement {

    private val _annotations = ArrayList<JavaAnnotation>()

    override val annotations: List<JavaAnnotation>
        get() = _annotations

    fun addAnnotation(annotation: JavaAnnotation) {
        _annotations += annotation
    }

    override fun toString(): String = buildString {
        append(type).append(' ').append(name)
    }
}
