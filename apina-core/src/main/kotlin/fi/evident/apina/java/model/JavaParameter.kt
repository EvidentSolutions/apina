package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import java.util.*

/**
 * Parameter definition for a [JavaMethod].
 */
class JavaParameter(val type: JavaType) : JavaAnnotatedElement {

    var name: String? = null

    private val _annotations = ArrayList<JavaAnnotation>()

    override val annotations: List<JavaAnnotation>
        get() = _annotations

    override fun toString() = "$type ${name ?: "<unknown>"}"

    fun addAnnotation(annotation: JavaAnnotation) {
        this._annotations += annotation
    }
}
