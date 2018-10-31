package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.Endpoint
import fi.evident.apina.model.EnumDefinition
import fi.evident.apina.model.parameters.EndpointParameter
import fi.evident.apina.model.parameters.EndpointPathVariableParameter
import fi.evident.apina.model.parameters.EndpointRequestParamParameter
import fi.evident.apina.model.settings.EnumMode
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import java.lang.String.format
import java.util.*

abstract class AbstractTypeScriptGenerator(
    val api: ApiDefinition,
    val settings: TranslationSettings,
    private val typePrefix: String,
    private val supportPrefix: String,
    private val resultFunctor: String
) {

    internal val out = CodeWriter()

    protected fun writeTypes() {

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

    private fun qualifiedTypeName(type: ApiType): String = when {
        type is ApiType.Nullable -> qualifiedTypeName(type.type) + " | null"
        type is ApiType.Primitive -> type.typeRepresentation()
        type is ApiType.Array -> qualifiedTypeName(type.elementType) + "[]"
        settings.isImported(ApiTypeName(type.typeRepresentation())) -> type.typeRepresentation()
        else -> typePrefix + type.typeRepresentation()
    }

    private fun writeSerializerDefinitions() {
        out.write("export function registerDefaultSerializers(config: " + supportPrefix + "ApinaConfig) ").writeBlock {
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

    private fun writeEnumSerializer(enumDefinition: EnumDefinition) {
        val enumName = enumDefinition.type.toString()
        when (settings.enumMode) {
            EnumMode.INT_ENUM ->
                out.write("config.registerEnumSerializer(").writeValue(enumName).write(", ").write(enumName).writeLine(");")
            EnumMode.DEFAULT, EnumMode.STRING_UNION ->
                out.write("config.registerIdentitySerializer(").writeValue(enumName).writeLine(");")
        }
    }

    protected fun endpointSignature(endpoint: Endpoint): String {
        val name = endpoint.name
        val parameters = parameterListCode(endpoint.parameters)
        val resultType = endpoint.responseBody?.let { this.qualifiedTypeName(it) } ?: "void"

        return format("%s(%s): %s<%s>", name, parameters, resultFunctor, resultType)
    }

    private fun parameterListCode(parameters: List<EndpointParameter>) =
        parameters.joinToString(", ") { p -> p.name + ": " + qualifiedTypeName(p.type) }

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
