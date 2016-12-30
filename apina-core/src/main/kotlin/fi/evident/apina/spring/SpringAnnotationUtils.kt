package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotation

internal object SpringAnnotationUtils {
    fun getRequestParamName(annotation: JavaAnnotation): String? =
        valueOrSecondaryAttribute(annotation, "name")

    fun getRequestMappingPath(annotation: JavaAnnotation): String? =
        valueOrSecondaryAttribute(annotation, "path")

    private fun valueOrSecondaryAttribute(annotation: JavaAnnotation, secondaryName: String): String? =
        annotation.getAttribute<String>("value") ?: annotation.getAttribute<String>(secondaryName)
}
