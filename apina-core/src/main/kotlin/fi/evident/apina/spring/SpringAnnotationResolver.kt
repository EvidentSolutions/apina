package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotatedElement
import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import java.util.*

/**
 * Implements Spring-compatible annotation resolving. This means that resolution
 * is aware of meta-annotations and `@AliasFor` and uses them to provide values
 * transparently.
 */
internal class SpringAnnotationResolver(private val javaModel: JavaModel) {

    /**
     * Returns annotation which is specified either directly or by meta-annotation. Note that
     * if annotation is specified by meta-annotation, then the returned annotation is not
     * itself `annotationType`.
     */
    fun findAnnotation(element: JavaAnnotatedElement, annotationType: JavaType.Basic): SpringAnnotation? {
        for (ann in element.annotations) {
            val implied = findImpliedAnnotations(ann)
            if (implied.any { it.name == annotationType })
                return SpringAnnotation(annotationType, implied, javaModel)
        }

        return null
    }

    fun findImpliedAnnotations(annotation: JavaAnnotation): Set<JavaAnnotation> {
        val implied = LinkedHashSet<JavaAnnotation>()

        fun recurse(ann: JavaAnnotation) {
            if (implied.add(ann)) {
                val cl = javaModel.findClass(ann.name)
                if (cl != null)
                    for (a in cl.annotations)
                        recurse(a)
            }
        }

        recurse(annotation)
        return implied
    }

    /**
     * Does this element have an given annotation either directly or through meta-annotation?
     */
    fun hasAnnotation(element: JavaAnnotatedElement, annotationType: JavaType.Basic) =
        findAnnotation(element, annotationType) != null
}

