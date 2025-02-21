package fi.evident.apina.model.settings

import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.spring.NameTranslator
import fi.evident.apina.utils.PatternSet
import java.util.*

/**
 * Various settings guiding the translation.
 */
class TranslationSettings {

    val blackBoxClasses = PatternSet()
    private val controllersToProcess = PatternSet()
    private val endpointUrlMethods = PatternSet()
    private val importsByModule = TreeMap<String, ImportDefinition>()
    private val importedTypes = TreeSet<ApiTypeName>()
    val brandedPrimitiveTypes = mutableListOf<BrandedPrimitiveType>()
    val nameTranslator = NameTranslator()
    var platform = Platform.ANGULAR

    @Deprecated("Writing types as interfaces is preferred and selection will be removed in future")
    var typeWriteMode = TypeWriteMode.INTERFACE
    var optionalTypeMode = OptionalTypeMode.NULL
    var enumMode = EnumMode.DEFAULT
    var removedUrlPrefix = ""
    var reexportImports = false

    fun isBlackBoxClass(name: String) = name in blackBoxClasses

    fun addImport(moduleName: String, types: Collection<String>) {
        val importDefinition = importsByModule.getOrPut(moduleName) { ImportDefinition(moduleName) }

        for (type in types) {
            val typeName = ApiTypeName(type)
            if (!importedTypes.add(typeName))
                throw IllegalArgumentException("type $typeName is already imported")

            importDefinition.addType(typeName)
        }
    }

    val imports: Collection<ImportDefinition>
        get() = importsByModule.values

    fun isImportedOrBrandedType(typeName: ApiTypeName) =
        typeName in importedTypes || brandedPrimitiveTypes.any { typeName == it.brandedType }

    fun isProcessableController(name: String) =
        controllersToProcess.isEmpty || name in controllersToProcess

    fun isUrlEndpoint(className: String, methodName: String) =
        "$className.$methodName" in endpointUrlMethods

    fun addControllerPattern(pattern: String) {
        controllersToProcess.addPattern(pattern)
    }

    fun addEndpointUrlMethodPattern(pattern: String) {
        endpointUrlMethods.addPattern(pattern)
    }

    fun normalizeUrl(url: String): String =
        url.removePrefix(removedUrlPrefix)

}
