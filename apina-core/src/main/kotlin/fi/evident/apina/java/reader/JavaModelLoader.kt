package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.system.measureTimeMillis

private object JavaModelLoader
private val log = LoggerFactory.getLogger(JavaModelLoader::class.java)

fun Classpath.loadModel(): JavaModel {
    val classes = JavaModel()

    val duplicates = LinkedHashSet<JavaType>()

    val processingMillis = measureTimeMillis {
        processAllClasses { inputStream ->
            val aClass = ClassMetadataReader.loadMetadata(inputStream)
            if (!classes.containsClass(aClass.name)) {
                classes.addClass(aClass)
            } else {
                duplicates += aClass.type
            }
        }
    }

    log.debug("Loaded {} classes in {} ms", classes.classes.size, processingMillis)

    if (duplicates.any()) {
        log.warn("There were {} classes with multiple definitions in classpath. Ignoring duplicate definitions.", duplicates.size)
        log.debug("Classes with multiple definitions: {}", duplicates)
    }

    return classes
}
