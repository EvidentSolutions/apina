package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaClass
import fi.evident.apina.utils.SkipZipPrefixStream
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.function.Supplier
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED

class ClasspathClassDataLoader(classpath: Classpath) : ClassDataLoader, Closeable {

    private val classSuppliers = mutableMapOf<String, Supplier<JavaClass>>()
    private val duplicates = mutableSetOf<String>()
    private val resources = mutableListOf<Closeable>()

    init {
        for (root in classpath.roots)
            loadAllClasses(root.toFile())
    }

    override fun close() {
        resources.forEach { it.close() }
    }

    override val classNames: Set<String>
        get() = classSuppliers.keys.toSet()

    val duplicateClassNames: Set<String>
        get() = duplicates

    override fun loadClass(name: String): JavaClass? =
        classSuppliers[name]?.get()

    private fun loadAllClasses(path: File) {
        log.debug("Scanning for classes in {}", path)

        when {
            path.isDirectory ->
                scanClassesUnderDirectory(path)

            path.isFile && path.hasExtension(".jar", ".war") ->
                scanClassesInArchiveFile(path)

            !path.exists() ->
                log.debug("Skipping nonexistent classpath entry: {}", path)

            else ->
                log.warn("Unknown classpath entry: $path")
        }
    }

    /**
     * When scanning top-level archives, we can easily get listing of files without
     * reading the whole archive. We simply read the archive directory listing and
     * keep pointers to the entries so we can read them lazily later on if necessary.
     */
    private fun scanClassesInArchiveFile(path: File) {
        val jar = JarFile(path)
        resources += jar

        for (entry in jar.entries()) {
            if (entry.name.isProcessedClassFile()) {
                log.trace("Processing class-file {} from {}", entry.name, path)
                addClassSupplier(entry.name.toClassName(), JarClassDataLoader(jar, entry))

            } else if (entry.isNestedArchive()) {
                log.trace("Processing nested library {}", entry.name)
                scanClassesInNestedArchive(jar.getInputStream(entry))
            }
        }
    }

    /**
     * For nested archives, we can't do similar optimizations as for top-level
     * archives. We read class bytes into memory so we can load the classes if needed.
     */
    private fun scanClassesInNestedArchive(stream: InputStream) {
        val jar = JarInputStream(SkipZipPrefixStream(stream))
        while (true) {
            val entry = jar.nextEntry ?: break

            if (entry.name.isProcessedClassFile()) {
                log.trace("Processing class-file {} from {}", entry.name, stream)
                addClassSupplier(entry.name.toClassName(), LazyClassData(jar.readBytes()))

            } else if (entry.isNestedArchive()) {
                log.trace("Processing nested library {}", entry.name)
                scanClassesInNestedArchive(jar)
            }
        }
    }

    private fun scanClassesUnderDirectory(dir: File) {
        for (file in dir.walk()) {
            if (file.isFile && file.name.isProcessedClassFile()) {
                log.trace("Processing class-file {}", file)

                addClassSupplier(file.toRelativeString(dir).toClassName(), FileClassDataLoader(file))
            }
        }
    }

    private fun addClassSupplier(className: String, supplier: Supplier<JavaClass>) {
        val old = classSuppliers.putIfAbsent(className, supplier)
        if (old != null)
            duplicates += className
    }

    private class LazyClassData(bytes: ByteArray) : Supplier<JavaClass> {
        private val data: JavaClass by lazy(SYNCHRONIZED) { ClassMetadataReader.loadMetadata(bytes.inputStream()) }

        override fun get() = data
    }

    private class JarClassDataLoader(private val jar: JarFile, private val entry: JarEntry) : Supplier<JavaClass> {
        private val data by lazy(SYNCHRONIZED) { ClassMetadataReader.loadMetadata(jar.getInputStream(entry)) }

        override fun get()  = data
    }

    private class FileClassDataLoader(private val file: File) : Supplier<JavaClass> {
        private val data  by lazy(SYNCHRONIZED) { file.inputStream().use { ClassMetadataReader.loadMetadata(it) } }

        override fun get() = data
    }

    companion object {

        private val log = LoggerFactory.getLogger(ClasspathClassDataLoader::class.java)

        private fun File.hasExtension(vararg extensions: String) =
            extensions.any { name.endsWith(it) }

        private fun String.toClassName() =
            removePrefix("WEB-INF/classes/").removePrefix("BOOT-INF/classes/").removeSuffix(".class").replace('/', '.').replace('\\', '.')

        private fun String.isProcessedClassFile() =
            endsWith(".class") && this != "module-info.class" && !startsWith("META-INF/versions")

        private fun ZipEntry.isNestedArchive() =
            name.endsWith(".jar") && (name.startsWith("lib/") || name.startsWith("WEB-INF/lib/") || name.startsWith("BOOT-INF/lib"))
    }
}
