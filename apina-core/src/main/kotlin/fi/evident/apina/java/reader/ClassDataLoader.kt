package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaClass

interface ClassDataLoader {
    val classNames: Set<String>
    fun loadClass(name: String): JavaClass?
}
