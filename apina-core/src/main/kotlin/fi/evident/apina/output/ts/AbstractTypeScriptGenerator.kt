package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.Endpoint
import fi.evident.apina.model.EndpointGroup
import fi.evident.apina.model.EnumDefinition
import fi.evident.apina.model.parameters.EndpointParameter
import fi.evident.apina.model.parameters.EndpointPathVariableParameter
import fi.evident.apina.model.parameters.EndpointRequestParamParameter
import fi.evident.apina.model.settings.EnumMode
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.utils.readResourceAsString
import java.lang.String.format
import java.util.*

abstract class AbstractTypeScriptGenerator(
    val api: ApiDefinition,
    val settings: TranslationSettings,
    private val resultFunctor: String,
    private val classDecorator: String
) {

    internal val out = CodeWriter()

    fun writeApi() {
        writeHeader()
        writeImports()
        writePlatformSpecificImports()
        writeTypes()
        writeCommonRuntime()
        writeRuntime()
        writeEndpoints(api.endpointGroups)
        writePlatformSpecific()
    }

    private fun writeHeader() {
        // The project using our generated code might generate the code to directory where
        // it's checked by TSLint and we don't know what settings are used so just disable
        // TSLint for the whole file.
        out.writeLine("/* tslint:disable */")
    }

    private fun writeImports() {
        val imports = settings.imports

        if (!imports.isEmpty()) {
            for (anImport in imports)
                out.writeLine("import { " + anImport.types.joinToString(", ") + " } from '" + anImport.moduleName + "';")

            out.writeLine()
        }
    }

    protected open fun writePlatformSpecificImports() {}

    private fun writeTypes() {

        out.writeExportedInterface("Dictionary<V>") { out.writeLine("[key: string]: V;") }

        for (unknownType in api.allBlackBoxClasses)
            out.writeLine("export type $unknownType = {};")

        out.writeLine()

        for (enumDefinition in api.enumDefinitions)
            writeEnum(enumDefinition)

        out.writeLine()

        for (classDefinition in api.classDefinitions) {
            out.writeExportedClass(classDefinition.type.toString()) {
                for (property in classDefinition.properties)
                    out.writeLine("${property.name}: ${property.type};")
            }
        }

        writeSerializerDefinitions()
    }

    private fun writeEnum(enumDefinition: EnumDefinition) {
        val constants = enumDefinition.constants
        out.writeLine(when (settings.enumMode) {
            EnumMode.DEFAULT ->
                format("export enum %s { %s }", enumDefinition.type, constants.joinToString(", ") { "$it = \"$it\"" })
            EnumMode.STRING_UNION ->
                format("export type %s = %s;", enumDefinition.type, constants.joinToString(" | ") { "\"$it\"" })
            EnumMode.INT_ENUM ->
                format("export enum %s { %s }", enumDefinition.type, constants.joinToString(", "))
        })
    }

    private fun writeSerializerDefinitions() {
        fun writeEnumSerializer(enumDefinition: EnumDefinition) {
            val enumName = enumDefinition.type.toString()
            when (settings.enumMode) {
                EnumMode.INT_ENUM ->
                    out.write("config.registerEnumSerializer(").writeValue(enumName).write(", ").write(enumName).writeLine(");")
                EnumMode.DEFAULT, EnumMode.STRING_UNION ->
                    out.write("config.registerIdentitySerializer(").writeValue(enumName).writeLine(");")
            }
        }

        out.write("export function registerDefaultSerializers(config: ApinaConfig) ").writeBlock {
            for (unknownType in api.allBlackBoxClasses)
                out.write("config.registerIdentitySerializer(").writeValue(unknownType.toString()).writeLine(");")
            out.writeLine()

            for (enumDefinition in api.enumDefinitions) {
                writeEnumSerializer(enumDefinition)
            }
            out.writeLine()

            for (classDefinition in api.classDefinitions) {
                val defs = LinkedHashMap<String, String>()

                for (property in classDefinition.properties)
                    defs[property.name] = typeDescriptor(property.type)

                out.write("config.registerClassSerializer(").writeValue(classDefinition.type.toString()).write(", ")
                out.writeValue(defs).writeLine(");")
                out.writeLine()
            }
        }

        out.writeLine().writeLine()
    }

    private fun writeCommonRuntime() {
        out.write(readResourceAsString("typescript/runtime-common.ts"))
        out.writeLine()
    }

    protected abstract fun writeRuntime()

    private fun writeEndpoints(endpointGroups: Collection<EndpointGroup>) {
        for (endpointGroup in endpointGroups) {
            if (classDecorator.isNotBlank()) {
                out.writeLine(classDecorator)
            }
            out.writeBlock("export class " + endpointGroup.name + "Endpoint") {

                out.writeBlock("constructor(private context: ApinaEndpointContext)") { }

                for (endpoint in endpointGroup.endpoints) {
                    writeEndpoint(endpoint)
                    out.writeLine().writeLine()
                }
            }
        }
    }

    private fun writeEndpoint(endpoint: Endpoint) {
        out.write(endpointSignature(endpoint)).write(" ").writeBlock { out.write("return this.context.request(").writeValue(AbstractTypeScriptGenerator.createConfig(endpoint)).writeLine(");") }
    }

    private fun endpointSignature(endpoint: Endpoint): String {
        val name = endpoint.name
        val parameters = parameterListCode(endpoint.parameters)
        val resultType = endpoint.responseBody?.let { qualifiedTypeName(it) } ?: "void"

        return format("%s(%s): %s<%s>", name, parameters, resultFunctor, resultType)
    }

    private fun qualifiedTypeName(type: ApiType): String = when {
        type is ApiType.Nullable -> qualifiedTypeName(type.type) + " | null"
        type is ApiType.Primitive -> type.typeRepresentation()
        type is ApiType.Array -> qualifiedTypeName(type.elementType) + "[]"
        settings.isImported(ApiTypeName(type.typeRepresentation())) -> type.typeRepresentation()
        else -> type.typeRepresentation()
    }

    private fun parameterListCode(parameters: List<EndpointParameter>) =
        parameters.joinToString(", ") { p -> p.name + ": " + qualifiedTypeName(p.type) }

    protected open fun writePlatformSpecific() {}

    companion object {

        @JvmStatic
        protected fun createConfig(endpoint: Endpoint): Map<String, Any> {
            val config = LinkedHashMap<String, Any>()

            config["uriTemplate"] = endpoint.uriTemplate.toString()
            config["method"] = endpoint.method.toString()

            val pathVariables = endpoint.pathVariables
            if (!pathVariables.isEmpty())
                config["pathVariables"] = createPathVariablesMap(pathVariables)

            val requestParameters = endpoint.requestParameters
            if (!requestParameters.isEmpty())
                config["requestParams"] = createRequestParamMap(requestParameters)

            endpoint.requestBody?.let { body -> config["requestBody"] = serialize(body.name, body.type.unwrapNullable()) }
            endpoint.responseBody?.let { body -> config["responseType"] = typeDescriptor(body) }

            return config
        }

        private fun createRequestParamMap(parameters: Collection<EndpointRequestParamParameter>): Map<String, Any> {
            val result = LinkedHashMap<String, Any>()

            for (param in parameters)
                result[param.queryParameter] = serialize(param.name, param.type)

            return result
        }

        private fun createPathVariablesMap(pathVariables: List<EndpointPathVariableParameter>): Map<String, Any> {
            val result = LinkedHashMap<String, Any>()

            for (param in pathVariables)
                result[param.pathVariable] = serialize(param.name, param.type)

            return result
        }

        /**
         * Returns TypeScript code to serialize `variable` of given `type`
         * to transfer representation.
         */
        private fun serialize(variable: String, type: ApiType) =
            RawCode("this.context.serialize(" + variable + ", '" + typeDescriptor(type) + "')")

        private fun typeDescriptor(type: ApiType): String {
            // Use ApiType's native representation as type descriptor.
            // This method encapsulates the call to make it meaningful in this context.
            return type.unwrapNullable().typeRepresentation()
        }
    }
}
