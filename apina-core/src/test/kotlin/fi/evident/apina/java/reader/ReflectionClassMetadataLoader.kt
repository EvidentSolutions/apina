package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaModel

import fi.evident.apina.java.reader.ClassReaderUtils.loadClass

object ReflectionClassMetadataLoader {

    @JvmStatic
    fun loadClassesFromInheritanceTree(clazz: Class<*>): JavaModel {
        val result = JavaModel()

        var cl: Class<*>? = clazz
        while (cl != null && cl != Any::class.java) {
            loadClassesAt(result, cl)
            cl = cl.superclass
        }

        return result
    }

    private fun loadClassesAt(result: JavaModel, cl: Class<*>) {
        if (!result.containsClass(cl.name)) {
            result.addClass(loadClass(cl))

            for (annotation in cl.declaredAnnotations)
                loadClassesAt(result, annotation.annotationClass.java)

            for (interfaceClass in cl.interfaces)
                loadClassesAt(result, interfaceClass)
        }
    }
}
