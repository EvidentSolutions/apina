package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import java.util.*

/**
 * Wrapper for annotations that performs Spring-specific lookup for attributes.
 */
class SpringAnnotation(
        /** The original annotation type that caller was interested in */
        private val annotationType: JavaType.Basic,
        /** The set of annotations that were actually implied by site, ordered by specificity */
        private val annotations: Collection<JavaAnnotation>,
        private val javaModel: JavaModel) {

    inline fun <reified T : Any> getAttribute(attributeName: String): T? = getAttribute(attributeName, T::class.java)
    inline fun <reified T : Any> getUniqueAttributeValue(attributeName: String): T? = getUniqueAttributeValue(attributeName, T::class.java)

    /**
     * Tries to find value for given attribute, considering meta-annotations and `@AliasFor`.
     */
    fun <T> getAttribute(attributeName: String, type: Class<T>): T? {
        for (annotation in annotations)
            return getAttributeFrom(annotation, attributeName, type) ?: continue

        return null
    }

    fun <T> getUniqueAttributeValue(attributeName: String, type: Class<T>): T? {
        val value = getAttribute<Any>(attributeName)

        return if (value is Array<*>) {
            when (value.size) {
                0 -> null
                1 -> type.cast(value[0])
                else -> throw IllegalArgumentException("multiple values for $attributeName in $this")
            }
        } else {
            type.cast(value)
        }
    }

    private fun <T> getAttributeFrom(annotation: JavaAnnotation, attributeName: String, type: Class<T>): T? {
        if (annotation.name == annotationType) {
            val value = annotation.getAttribute(attributeName, type)
            if (value != null)
                return value
        }

        val aliases = findAliases(annotation.name)
        for (alias in aliases) {
            if (alias.matches(annotationType, attributeName))
                return annotation.getAttribute(alias.sourceAttribute, type) ?: continue
        }

        return null
    }

    /**
     * Returns all aliases defined by given annotation type.
     */
    private fun findAliases(annotationType: JavaType.Basic): Collection<AliasFor> {
        val clazz = javaModel.findClass(annotationType)
        if (clazz != null) {
            val aliases = ArrayList<AliasFor>()
            for (m in clazz.methods) {
                val aliasFor = m.findAnnotation(AliasFor.TYPE)
                if (aliasFor != null) {
                    val attribute = aliasFor.getAttribute<String>("attribute") ?: aliasFor.getAttribute<String>("value") ?: m.name
                    val annotation = aliasFor.getAttribute<JavaType.Basic>("annotation") ?: annotationType

                    aliases += AliasFor(annotationType, m.name, annotation, attribute)
                }
            }
            return aliases
        } else {
            return emptyList()
        }
    }
}
