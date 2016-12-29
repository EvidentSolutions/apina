package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import java.util.*

/**
 * Contains metadata for all loaded classes.
 */
class JavaModel {

    private val classes = LinkedHashMap<String, JavaClass>()

    fun addClass(aClass: JavaClass) {
        val old = classes.putIfAbsent(aClass.name, aClass)
        if (old != null)
            throw IllegalStateException("Class ${aClass.name} was already added")
    }

    fun containsClass(name: String) = name in classes

    fun findClass(name: String): JavaClass? = classes[name]

    fun findClass(type: JavaType.Basic): JavaClass? = findClass(type.name)

    fun findClassesWithAnnotation(annotationType: JavaType.Basic): List<JavaClass> =
            classes.values.filter { it.hasAnnotation(annotationType) }

    /**
     * Return types for annotations implied by given annotation using same logic as Spring does.
     * E.g. if annotation `@Foo` is itself annotated by `@Bar`, then annotating an
     * element with `@Foo` has the same effect as annotation by `@Bar`.
     */
    fun findAnnotationsImpliedBy(annotationType: JavaType.Basic): Set<JavaType.Basic> {
        val result = LinkedHashSet<JavaType.Basic>()

        findAnnotationsImpliedBy(annotationType, result)

        return result
    }

    private fun findAnnotationsImpliedBy(annotationType: JavaType.Basic, result: MutableSet<JavaType.Basic>) {
        if (result.add(annotationType)) {

            for (cl in classes.values)
                if (cl.isAnnotation && cl.hasAnnotation(annotationType))
                    findAnnotationsImpliedBy(cl.type.toBasicType(), result)
        }
    }

    inline fun <reified T : Any> isInstanceOf(type: JavaType) = isInstanceOf(type, T::class.java)

    fun isInstanceOf(type: JavaType, requiredType: Class<*>): Boolean {
        val javaClass = classes[type.nonGenericClassName]

        if (javaClass != null) {
            return javaClass.name == requiredType.name
                    || isInstanceOf(javaClass.superClass, requiredType)
                    || javaClass.interfaces.any { isInstanceOf(it, requiredType) }

        } else if (type is JavaType.Basic) {
            try {
                val aClass = Class.forName(type.name)
                return requiredType.isAssignableFrom(aClass)

            } catch (e: ClassNotFoundException) {
                return false
            }

        } else {
            return false
        }
    }

    fun isNumber(type: JavaType.Basic) = type.isPrimitiveNumber || isInstanceOf<Number>(type)

    override fun toString() = classes.toString()
}
