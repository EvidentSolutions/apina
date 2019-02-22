package fi.evident.apina

import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.model.settings.Platform.*
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.output.ts.TypeScriptAngularGenerator
import fi.evident.apina.output.ts.TypeScriptES6Generator
import fi.evident.apina.spring.SpringModelReader
import org.slf4j.LoggerFactory

class ApinaProcessor(private val classpath: Classpath) {

    val settings = TranslationSettings()

    fun process(): String {
        val api = SpringModelReader.readApiDefinition(classpath, settings)

        log.debug("Loaded {} endpoint groups with {} endpoints.", api.endpointGroupCount, api.endpointCount)
        log.trace("Loaded endpoint groups: {}", api.endpointGroups)

        if (api.endpointCount == 0) {
            log.warn("Apina could not find any endpoints to process")
        }

        log.debug("Loaded {} class definitions", api.classDefinitionCount)
        log.trace("Loaded class definitions: {}", api.classDefinitions)

        log.debug("Loaded {} enum definitions", api.enumDefinitionCount)
        log.trace("Loaded enum definitions: {}", api.enumDefinitions)

        val unknownTypes = api.unknownTypeReferences
        if (!unknownTypes.isEmpty()) {
            log.warn("Writing {} unknown class definitions as black boxes: {}", unknownTypes.size, unknownTypes)
        }

        @Suppress("DEPRECATION")
        val writer = when (settings.platform) {
            ANGULAR -> TypeScriptAngularGenerator(api, settings)
            ANGULAR2 -> {
                log.warn("Platform.ANGULAR2 is deprecated, use Platform.ANGULAR instead")
                TypeScriptAngularGenerator(api, settings)
            }
            ES6 -> TypeScriptES6Generator(api, settings)
        }

        writer.writeApi()
        return writer.output
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApinaProcessor::class.java)
    }
}
