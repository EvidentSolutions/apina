package fi.evident.apina.output.ts

import fi.evident.apina.model.*
import fi.evident.apina.model.parameters.EndpointParameter
import fi.evident.apina.model.settings.EnumMode
import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.settings.TypeWriteMode
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.output.common.RawCode
import fi.evident.apina.utils.readResourceAsString
import java.lang.String.format

abstract class AbstractTypeScriptGenerator(
    val api: ApiDefinition,
    val settings: TranslationSettings,
    private val resultFunctor: ResultFunctor,
    private val classDecorator: String,
    private val platformRuntimeCodePath: String,
    private val platformSpecificImports: Map<String, List<String>> = emptyMap()
) {

    private val out = TypeScriptWriter()

    val output: String
        get() = out.output

    fun writeApi() {
        writeHeader()
        writeImports()
        writeTypes()
        writeResource("typescript/runtime-common.ts")
        writeResource(platformRuntimeCodePath)
        writeEndpoints(api.endpointGroups)
        writeApinaConfig()
    }

    private fun writeHeader() {
        // The project using our generated code might generate the code to directory where
        // it's checked by TSLint and we don't know what settings are used so just disable
        // TSLint for the whole file.
        out.writeLine("/* tslint:disable */")
    }

    private fun writeImports() {
        val imports = settings.imports

        if (imports.isNotEmpty()) {
            for (anImport in imports)
                out.writeImport(anImport.moduleName, anImport.types.map { it.name })

            out.writeLine()
        }

        if (platformSpecificImports.isNotEmpty()) {
            for ((module, imps) in platformSpecificImports)
                out.writeImport(module, imps)

            out.writeLine()
        }

        if (settings.reexportImports && imports.isNotEmpty()) {
            for (anImport in imports)
                out.writeExport(anImport.types.map { it.name })

            out.writeLine()
        }
    }

    private fun writeTypes() {

        for (type in settings.brandedPrimitiveTypes)
            out.writeLine("export type ${type.brandedType.name} = Branded<${type.implementationType.toTypeScript()}, '${type.brandedType.name}'>;")

        for (type in api.allBlackBoxClasses)
            out.writeLine("export type ${type.name} = {};")

        for ((alias, target) in api.typeAliases)
            out.writeLine("export type ${alias.name} = ${target.toTypeScript()};")

        out.writeLine()

        for (enumDefinition in api.enumDefinitions)
            writeEnum(enumDefinition)

        out.writeLine()

        val classDefinitionWriter = when (settings.typeWriteMode) {
            TypeWriteMode.CLASS -> out::writeExportedClass
            TypeWriteMode.INTERFACE -> out::writeExportedInterface
        }

        for (classDefinition in api.structuralTypeDefinitions) {
            classDefinitionWriter(classDefinition.type.name) {
                for (property in classDefinition.properties)
                    if (settings.optionalTypeMode == OptionalTypeMode.UNDEFINED && property.type is ApiType.Nullable) {
                        out.writeLine("${property.name}?: ${property.type.type.toTypeScript()};")
                    } else {
                        out.writeLine("${property.name}: ${property.type.toTypeScript()};")
                    }
            }
        }

        for (definition in api.discriminatedUnionDefinitions)
            writeDiscriminatedUnion(definition)
    }

    private fun writeEnum(enumDefinition: EnumDefinition) {
        val typeName = enumDefinition.type.name
        val constants = enumDefinition.constants
        out.writeLine(
            when (settings.enumMode) {
                EnumMode.DEFAULT ->
                    format("export enum %s { %s }", typeName, constants.joinToString(", ") { "$it = \"$it\"" })

                EnumMode.STRING_UNION ->
                    format("export type %s = %s;", typeName, constants.joinToString(" | ") { "\"$it\"" })

                EnumMode.INT_ENUM ->
                    format("export enum %s { %s }", typeName, constants.joinToString(", "))
            }
        )
    }

    private fun writeDiscriminatedUnion(definition: DiscriminatedUnionDefinition) {
        // First individual members of the union...
        for ((discriminatorValue, member) in definition.types) {
            val typeName = discriminatedUnionMemberType(definition.type, member)
            out.writeBlock("export interface $typeName extends ${member.type.name}") {
                out.writeLine("${definition.discriminator}: '$discriminatorValue';")
            }
            out.writeLine()
        }

        // ...then the union itself
        val members = definition.types.values.map { discriminatedUnionMemberType(definition.type, it) }
        out.writeLine("export type ${definition.type.name} = ${members.joinToString(" | ")};")
        out.writeLine()
    }

    private fun writeEnumSerializer(enumDefinition: EnumDefinition) {
        when (settings.enumMode) {
            EnumMode.INT_ENUM -> {
                val enumName = enumDefinition.type.name
                out.write("this.registerEnumSerializer(").writeValue(enumName).write(", ").write(enumName)
                    .writeLine(");")
            }

            EnumMode.DEFAULT, EnumMode.STRING_UNION ->
                writeIdentitySerializer(enumDefinition.type)
        }
    }

    private fun writeIdentitySerializer(type: ApiTypeName) {
        out.write("this.registerIdentitySerializer(").writeValue(type.name).writeLine(");")
    }

    private fun writeApinaConfig() {
        out.writeBlock("export class ApinaConfig extends ApinaConfigBase") {
            out.writeBlock("constructor()") {
                out.writeLine("super();")
                out.writeLine()

                writeSerializerDefinitions()
            }
        }
    }

    private fun writeSerializerDefinitions() {
        for (type in settings.brandedPrimitiveTypes)
            writeIdentitySerializer(type.brandedType)

        for (aliasedType in api.typeAliases.keys)
            writeIdentitySerializer(aliasedType)

        for (unknownType in api.allBlackBoxClasses)
            writeIdentitySerializer(unknownType)

        out.writeLine()

        for (enumDefinition in api.enumDefinitions)
            writeEnumSerializer(enumDefinition)

        out.writeLine()

        for (classDefinition in api.structuralTypeDefinitions) {
            val defs = LinkedHashMap<String, String>()

            for (property in classDefinition.properties)
                defs[property.name] = typeDescriptor(property.type)

            val typeName = classDefinition.type.name
            out.write("this.registerClassSerializer<$typeName>(").writeValue(typeName).write(", ")
            out.writeValue(defs).writeLine(");")
            out.writeLine()
        }

        for (definition in api.discriminatedUnionDefinitions) {
            val defs = mutableMapOf<String, String>()

            for ((discriminatorValue, type) in definition.types)
                defs[discriminatorValue] = typeDescriptor(ApiType.Class(type.type))

            val typeName = definition.type.name
            out.write("this.registerDiscriminatedUnionSerializer<$typeName>(")
            out.writeValue(typeName).write(", ")
            out.writeValue(definition.discriminator).write(", ")
            out.writeValue(defs).writeLine(");")
            out.writeLine()
        }
    }

    private fun writeResource(path: String) {
        out.write(readResourceAsString(path))
        out.writeLine()
    }

    private fun writeEndpoints(endpointGroups: Collection<EndpointGroup>) {
        for (endpointGroup in endpointGroups) {
            if (classDecorator.isNotBlank()) {
                out.writeLine(classDecorator)
            }
            out.writeBlock("export class ${endpointGroup.name}Endpoint") {

                out.writeBlock("constructor(private readonly context: ApinaEndpointContext)") { }

                for (endpoint in endpointGroup.endpoints) {
                    writeEndpoint(endpoint)
                    out.writeLine().writeLine()

                    if (endpoint.generateUrlMethod) {
                        writeUrlEndpoint(endpoint)
                        out.writeLine().writeLine()
                    }
                }
            }
        }
    }

    private fun writeEndpoint(endpoint: Endpoint) {
        val parameters = parameterListCode(endpoint.parameters)
        val resultType = endpoint.responseBody?.let { qualifiedTypeName(it) } ?: "void"
        val signature = "${endpoint.name}($parameters): ${resultFunctor.apply(resultType)}"

        out.write(signature).write(" ").writeBlock {
            out.write("return this.context.request(")
                .writeValue(createConfig(endpoint)).writeLine(");")
        }
    }

    private fun writeUrlEndpoint(endpoint: Endpoint) {
        val parameters = parameterListCode(endpoint.urlParameters)
        val signature = "${endpoint.name}Url($parameters): string"

        out.write(signature).write(" ").writeBlock {
            out.write("return this.context.url(")
                .writeValue(createConfig(endpoint, onlyUrl = true))
                .writeLine(");")
        }
    }

    private fun qualifiedTypeName(type: ApiType): String = when {
        type is ApiType.Nullable -> when (settings.optionalTypeMode) {
            OptionalTypeMode.UNDEFINED -> qualifiedTypeName(type.type) + " | undefined"
            OptionalTypeMode.NULL -> qualifiedTypeName(type.type) + " | null"
        }

        type is ApiType.Primitive -> type.toTypeScript()
        type is ApiType.Array -> qualifiedTypeName(type.elementType) + "[]"
        settings.isImportedOrBrandedType(ApiTypeName(type.toTypeScript())) -> type.toTypeScript()
        else -> type.toTypeScript()
    }

    private fun discriminatedUnionMemberType(unionType: ApiTypeName, memberType: ClassDefinition) =
        "${unionType.name}_${memberType.type.name}"

    private fun parameterListCode(parameters: List<EndpointParameter>): String {
        val firstOptionalIndex = parameters.indexOfLast { it.type !is ApiType.Nullable } + 1

        return parameters.withIndex().joinToString(", ") { (index, p) ->
            if (p.type is ApiType.Nullable) {
                val type = qualifiedTypeName(p.type.type)
                if (index >= firstOptionalIndex)
                    "${p.name}?: $type | null"
                else
                    "${p.name}: $type | null | undefined"
            } else {
                "${p.name}: ${qualifiedTypeName(p.type)}"
            }
        }
    }

    private fun createConfig(endpoint: Endpoint, onlyUrl: Boolean = false): Map<String, Any> = buildMap {
        put("uriTemplate", endpoint.uriTemplate.toString())

        if (!onlyUrl)
            put("method", endpoint.method.toString())

        val pathVariables = endpoint.pathVariables
        if (pathVariables.isNotEmpty())
            put("pathVariables", pathVariables.associate { it.pathVariable to serialize(it.name, it.type) })

        val requestParameters = endpoint.requestParameters
        if (requestParameters.isNotEmpty())
            put("requestParams", requestParameters.associate { it.queryParameter to serialize(it.name, it.type) })

        if (!onlyUrl) {
            endpoint.requestBody?.let { body ->
                put("requestBody", serialize(body.name, body.type.unwrapNullable()))
            }

            endpoint.responseBody?.let { body ->
                put("responseType", typeDescriptor(body))
            }
        }
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
        return type.unwrapNullable().toTypeScript()
    }

    private fun ApiType.toTypeScript(): String = toTypeScript(settings.optionalTypeMode)

    companion object {

        /**
         * When writing TypeScript, members of discriminated unions will produce
         * interface definitions, so we'll include them as logical class definitions
         * even though they are not in the model.
         */
        private val ApiDefinition.structuralTypeDefinitions: Collection<ClassDefinition>
            get() = classDefinitions + discriminatedUnionDefinitions
                .flatMap { du -> du.types.values }
                .distinctBy { it.type }
                .sortedBy { it.type }
    }
}

internal fun ApiType.toTypeScript(optionalTypeMode: OptionalTypeMode): String = when (this) {
    is ApiType.Array -> elementType.toTypeScript(optionalTypeMode) + "[]"
    is ApiType.BlackBox -> name.name
    is ApiType.Class -> name.name
    is ApiType.Dictionary -> "Record<string, ${valueType.toTypeScript(optionalTypeMode)}>"
    is ApiType.Nullable -> when (optionalTypeMode) {
        OptionalTypeMode.UNDEFINED -> type.toTypeScript(optionalTypeMode) + " | undefined"
        OptionalTypeMode.NULL -> type.toTypeScript(optionalTypeMode) + " | null"
    }

    is ApiType.Primitive -> toTypeScript()
}

private fun ApiType.Primitive.toTypeScript(): String = when (this) {
    ApiType.Primitive.ANY -> "any"
    ApiType.Primitive.STRING -> "string"
    ApiType.Primitive.BOOLEAN -> "boolean"
    ApiType.Primitive.INTEGER -> "number"
    ApiType.Primitive.FLOAT -> "number"
    ApiType.Primitive.VOID -> "void"
}
