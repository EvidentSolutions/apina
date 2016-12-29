package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaClass
import java.io.FileNotFoundException
import java.io.InputStream

object ClassReaderUtils {

    @JvmStatic
    fun loadClass(cl: Class<*>): JavaClass =
        openInputStreamForClassBytes(cl).use { ClassMetadataReader.loadMetadata(it) }

    private fun openInputStreamForClassBytes(cl: Class<*>): InputStream {
        val resourceName = cl.name.replace('.', '/') + ".class"
        var classLoader: ClassLoader? = cl.classLoader
        if (classLoader == null)
            classLoader = ClassLoader.getSystemClassLoader()

        val inputStream = classLoader!!.getResourceAsStream(resourceName)
        if (inputStream != null)
            return inputStream
        else
            throw FileNotFoundException(resourceName)
    }
}
