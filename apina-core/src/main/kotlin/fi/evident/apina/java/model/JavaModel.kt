package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import java.util.*

/**
 * Contains metadata for all loaded classes.
 */
class JavaModel {

    private val _classes = LinkedHashMap<String, JavaClass>()

    val classes: Collection<JavaClass>
        get() = _classes.values

    val annotationClasses: Sequence<JavaClass>
        get() = _classes.values.asSequence().filter { it.isAnnotation }

    fun addClass(aClass: JavaClass) {
        val old = _classes.putIfAbsent(aClass.name, aClass)
        if (old != null)
            throw IllegalStateException("Class ${aClass.name} was already added")
    }

    fun containsClass(name: String) = name in _classes

    fun findClass(name: String): JavaClass? = _classes[name]

    fun findClass(type: JavaType.Basic): JavaClass? = findClass(type.name)

    fun findClassesWithAnnotation(annotationType: JavaType.Basic): List<JavaClass> =
            _classes.values.filter { it.hasAnnotation(annotationType) }

    inline fun <reified T : Any> isInstanceOf(type: JavaType) = isInstanceOf(type, T::class.java)

    fun isInstanceOf(type: JavaType, requiredType: Class<*>): Boolean {
        val javaClass = _classes[type.nonGenericClassName]

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

    override fun toString() = _classes.toString()
}
