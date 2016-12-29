package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType

interface JavaAnnotatedElement {

    val annotations: List<JavaAnnotation>

    fun hasAnnotation(annotationType: JavaType.Basic)=
            findAnnotation(annotationType) != null

    fun findAnnotation(annotationType: JavaType.Basic): JavaAnnotation? =
            annotations.find { annotationType == it.name }

    fun getAnnotation(annotationType: JavaType.Basic): JavaAnnotation =
            findAnnotation(annotationType) ?: throw IllegalArgumentException("annotation not present: $annotationType")

    fun <T> findUniqueAnnotationAttributeValue(annotationType: JavaType.Basic, attributeName: String, type: Class<T>): T? =
            findAnnotation(annotationType)?.findUniqueAttributeValue(attributeName, type)

}
