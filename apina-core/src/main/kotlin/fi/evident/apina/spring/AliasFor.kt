package fi.evident.apina.spring

import fi.evident.apina.java.model.type.JavaType

/**
 * Representation for Spring's `AliasFor`.
 *
 * Tells that `sourceAttribute` of `sourceAnnotation` is an alias for `targetAttribute`
 * of `targetAnnotation`.
 */
data class AliasFor(val sourceAnnotation: JavaType.Basic,
                    val sourceAttribute: String,
                    val targetAnnotation: JavaType.Basic,
                    val targetAttribute: String) {

    fun matches(annotation: JavaType.Basic, attribute: String) =
            annotation == this.targetAnnotation && attribute == targetAttribute

    companion object {
        val TYPE = JavaType.Basic("org.springframework.core.annotation.AliasFor")
    }
}
