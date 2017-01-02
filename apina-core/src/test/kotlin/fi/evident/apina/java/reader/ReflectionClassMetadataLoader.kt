package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaModel

import fi.evident.apina.java.reader.ClassReaderUtils.loadClass

inline fun <reified T : Any> JavaModel.loadClassesFromInheritanceTree() {
    loadClassesFromInheritanceTree(T::class.java)
}

fun JavaModel.loadClassesFromInheritanceTree(clazz: Class<*>) {
    var cl: Class<*>? = clazz
    while (cl != null && cl != Any::class.java) {
        loadClassesAt(cl)
        cl = cl.superclass
    }
}

private fun JavaModel.loadClassesAt(cl: Class<*>) {
    if (!containsClass(cl.name)) {
        addClass(loadClass(cl))

        for (annotation in cl.declaredAnnotations)
            loadClassesAt(annotation.annotationClass.java)

        for (interfaceClass in cl.interfaces)
            loadClassesAt(interfaceClass)
    }
}

