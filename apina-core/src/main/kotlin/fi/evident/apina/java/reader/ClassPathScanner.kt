package fi.evident.apina.java.reader

import fi.evident.apina.utils.SkipZipPrefixStream
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Path
import java.util.jar.JarInputStream

private object ClassPathScanner

private val log = LoggerFactory.getLogger(ClassPathScanner::class.java)

/**
 * Executes given processor for all classes
 */
fun Classpath.processAllClasses(processor: (InputStream) -> Unit) {
    for (classpathEntry in roots)
        classpathEntry.processAllClasses(processor)
}

private fun Path.processAllClasses(processor: (InputStream) -> Unit) {
    log.debug("Scanning for classes in {}", this)

    when {
        Files.isDirectory(this) ->
            forEachClassUnderDirectory(processor)

        isRegularFile(this) && hasExtension(".jar") ->
            forEachClassInArchive(processor)

        Files.notExists(this) ->
            log.warn("Skipping nonexistent classpath entry: {}", this)

        else ->
            throw IOException("invalid classpath entry: $this")
    }
}

private fun Path.forEachClassInArchive(processor: (InputStream) -> Unit) {
    JarInputStream(SkipZipPrefixStream(Files.newInputStream(this))).use { jar ->
        while (true) {
            val entry = jar.nextEntry ?: break

            if (entry.name.endsWith(".class")) {
                log.trace("Processing class-file {} from {}", entry.name, this)
                processor(jar)
            }
        }
    }
}

private fun Path.forEachClassUnderDirectory(processor: (InputStream) -> Unit) {
    Files.walk(this)
            .filter { p -> isRegularFile(p) && p.toString().endsWith(".class") }
            .forEach { file ->
                log.trace("Processing class-file {}", file)

                BufferedInputStream(Files.newInputStream(file)).use(processor)
            }
}

private fun Path.hasExtension(vararg extensions: String): Boolean {
    val str = toString()
    return extensions.any { str.endsWith(it) }
}
