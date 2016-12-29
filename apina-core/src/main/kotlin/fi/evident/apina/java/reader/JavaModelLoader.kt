package fi.evident.apina.java.reader

import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import org.slf4j.LoggerFactory
import java.util.*

object JavaModelLoader {

    private val log = LoggerFactory.getLogger(JavaModelLoader::class.java)

    fun load(classpath: Classpath): JavaModel {
        val classes = JavaModel()

        val duplicates = LinkedHashSet<JavaType>()

        ClassPathScanner.processAllClasses(classpath) { inputStream ->
            val aClass = ClassMetadataReader.loadMetadata(inputStream)
            if (!classes.containsClass(aClass.name)) {
                classes.addClass(aClass)
            } else {
                duplicates += aClass.type
            }
        }

        if (duplicates.any()) {
            log.warn("There were {} classes with multiple definitions in classpath. Ignoring duplicate definitions.", duplicates.size)
            log.debug("Classes with multiple definitions: {}", duplicates)
        }

        return classes
    }
}
