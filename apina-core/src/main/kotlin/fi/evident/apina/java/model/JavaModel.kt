package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.reader.ClassDataLoader

/**
 * Contains metadata for all loaded classes.
 */
class JavaModel(private val classDataLoader: ClassDataLoader) {

    fun findClass(name: String): JavaClass? = classDataLoader.loadClass(name)

    fun findClass(type: JavaType.Basic): JavaClass? = findClass(type.name)

    private fun findClasses(namePredicate: (String) -> Boolean): List<JavaClass> =
        classDataLoader.classNames
            .filter { namePredicate(it) }
            .mapNotNull { classDataLoader.loadClass(it) }

    fun findClassesWithAnnotation(namePredicate: (String) -> Boolean, annotationType: JavaType.Basic): List<JavaClass> =
        findClasses(namePredicate)
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

    fun isIntegral(type: JavaType.Basic) =
        type.isPrimitiveIntegral || isInstanceOf<Short>(type) || isInstanceOf<Int>(type) || isInstanceOf<Long>(type)

    fun isNumber(type: JavaType.Basic) = type.isPrimitiveNumber || isInstanceOf<Number>(type)

    fun classesUpwardsFrom(javaClass: BoundClass): List<BoundClass> {
        val result = ArrayList<BoundClass>()

        fun recurse(c: BoundClass) {
            if (result.any { c.javaClass == it.javaClass })
                return

            result += c

            c.javaClass.interfaces
                .asSequence()
                .mapNotNull { boundClassFor(it, c.environment) }
                .forEach(::recurse)
        }

        var cl: BoundClass? = javaClass
        while (cl != null) {
            recurse(cl)
            cl = boundClassFor(cl.javaClass.superClass, cl.environment)
        }

        return result
    }

    private fun boundClassFor(type: JavaType, env: TypeEnvironment): BoundClass? =
        findClass(type.nonGenericClassName)?.let { c ->
            if (type is JavaType.Parameterized)
                BoundClass(c, c.schema.apply(type.arguments.map { it.resolve(env) }))
            else
                BoundClass(c, TypeEnvironment.empty())
        }

    fun findDirectSubclassesInSamePackage(cl: JavaClass): List<JavaClass> {
        val packageName = cl.type.packageName
        return findClasses { JavaType.packageNameForClassName(it) == packageName }
            .filter { it.superClass == cl.type }
    }
}
