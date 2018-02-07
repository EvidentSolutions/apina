package fi.evident.apina.spring

import fi.evident.apina.java.model.type.JavaType

/**
 * Representation for Spring's `AliasFor`.
 *
 * Tells that `sourceAttribute` of `sourceAnnotation` is an alias for `targetAttribute`
 * of `targetAnnotation`.
 */
data class AliasFor(
    private val sourceAnnotation: JavaType.Basic,
    val sourceAttribute: String,
    private val targets: Set<Pair<JavaType.Basic, String>>) {

    fun matches(annotation: JavaType.Basic, attribute: String) =
            (annotation == sourceAnnotation && attribute == sourceAttribute) || (annotation to attribute) in targets

    override fun toString() = "$sourceAnnotation.$sourceAttribute: $targets"
}
