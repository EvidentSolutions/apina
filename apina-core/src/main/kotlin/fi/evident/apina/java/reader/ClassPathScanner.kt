package fi.evident.apina.java.reader

import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Path
import java.util.jar.JarInputStream

/**
 * Reads class metadata for classes in classpath.
 */
internal object ClassPathScanner {

    private val log = LoggerFactory.getLogger(ClassPathScanner::class.java)

    /**
     * Executes given processor for all classes
     */
    fun processAllClasses(classpath: Classpath, processor: (InputStream) -> Unit) {
        for (classpathEntry in classpath.roots)
            processAllClasses(classpathEntry, processor)
    }

    private fun processAllClasses(classpathEntry: Path, processor: (InputStream) -> Unit) {
        log.debug("Scanning for classes in {}", classpathEntry)

        when {
            Files.isDirectory(classpathEntry) ->
                forEachClassUnderDirectory(classpathEntry, processor)

            isRegularFile(classpathEntry) && classpathEntry.toString().endsWith(".jar") ->
                forEachClassInArchive(classpathEntry, processor)

            Files.notExists(classpathEntry) ->
                log.warn("Skipping nonexistent classpath entry: {}", classpathEntry)

            else ->
                throw IOException("invalid classpath entry: $classpathEntry")
        }
    }

    private fun forEachClassInArchive(archivePath: Path, processor: (InputStream) -> Unit) {
        JarInputStream(Files.newInputStream(archivePath)).use { jar ->
            while (true) {
                val entry = jar.nextEntry ?: break

                if (entry.name.endsWith(".class")) {
                    log.trace("Processing class-file {} from {}", entry.name, archivePath)
                    processor(jar)
                }
            }
        }
    }

    private fun forEachClassUnderDirectory(directory: Path, processor: (InputStream) -> Unit) {
        Files.walk(directory)
                .filter { p -> isRegularFile(p) && p.toString().endsWith(".class") }
                .forEach { file ->
                    log.trace("Processing class-file {}", file)

                    BufferedInputStream(Files.newInputStream(file)).use(processor)
                }
    }
}
