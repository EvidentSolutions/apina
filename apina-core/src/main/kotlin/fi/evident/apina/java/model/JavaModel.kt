package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.reader.ClassDataLoader

/**
 * Contains metadata for all loaded classes.
 */
class JavaModel(private val classDataLoader: ClassDataLoader) {

    fun findClass(name: String): JavaClass? = classDataLoader.loadClass(name)

    fun findClass(type: JavaType.Basic): JavaClass? = findClass(type.name)

    fun findClassesWithAnnotation(namePredicate: (String) -> Boolean, annotationType: JavaType.Basic): List<JavaClass> =
        classDataLoader.classNames
            .filter { namePredicate(it) }
            .mapNotNull { classDataLoader.loadClass(it) }
            .filter { it.hasAnnotation(annotationType) }

    inline fun <reified T : Any> isInstanceOf(type: JavaType) = isInstanceOf(type, T::class.java)

    fun isInstanceOf(type: JavaType, requiredType: Class<*>): Boolean {
        if (type is JavaType.Wildcard)
            return type.upperBound != null && isInstanceOf(type.upperBound, requiredType)

        val javaClass = classDataLoader.loadClass(type.nonGenericClassName)

        return when {
            javaClass != null ->
                javaClass.name == requiredType.name
                        || isInstanceOf(javaClass.superClass, requiredType)
                        || javaClass.interfaces.any { isInstanceOf(it, requiredType) }

            type is JavaType.Basic ->
                try {
                    requiredType.isAssignableFrom(Class.forName(type.name))

                } catch (e: ClassNotFoundException) {
                    false
                }

            else ->
                false
        }
    }

    fun isNumber(type: JavaType.Basic) = type.isPrimitiveNumber || isInstanceOf<Number>(type)
}
