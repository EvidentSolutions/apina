package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaClass
import java.io.FileNotFoundException
import java.io.InputStream

fun loadClass(cl: Class<*>): JavaClass =
    openInputStreamForClassBytes(cl).use { ClassMetadataReader.loadMetadata(it) }

private fun openInputStreamForClassBytes(cl: Class<*>): InputStream {
    val resourceName = cl.name.replace('.', '/') + ".class"
    val classLoader = cl.classLoader ?: ClassLoader.getSystemClassLoader()

    return classLoader.getResourceAsStream(resourceName) ?: throw FileNotFoundException(resourceName)
}
