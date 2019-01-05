package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaClass

class TestClassMetadataLoader : ClassDataLoader {

    private val classes = mutableMapOf<String, JavaClass>()

    override val classNames: Set<String>
        get() = classes.keys.toSet()

    fun addClass(cl: JavaClass) {
        classes[cl.name] = cl
    }

    override fun loadClass(name: String): JavaClass? =
        classes[name]

    inline fun <reified T : Any> loadClassesFromInheritanceTree() {
        loadClassesFromInheritanceTree(T::class.java)
    }

    fun loadClassesFromInheritanceTree(clazz: Class<*>) {
        var cl: Class<*>? = clazz
        while (cl != null && cl != Any::class.java) {
            loadClassesAt(cl)
            cl = cl.superclass
        }
    }

    private fun loadClassesAt(cl: Class<*>) {
        if (!classes.containsKey(cl.name)) {
            addClass(loadClass(cl))

            for (annotation in cl.declaredAnnotations)
                loadClassesAt(annotation.annotationClass.java)

            for (interfaceClass in cl.interfaces)
                loadClassesAt(interfaceClass)
        }
    }
}
