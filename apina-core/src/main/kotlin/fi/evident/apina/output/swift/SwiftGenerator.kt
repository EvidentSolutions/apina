package fi.evident.apina.output.swift

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.DiscriminatedUnionDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType

class SwiftGenerator(val api: ApiDefinition, val settings: TranslationSettings) {

    private val out = SwiftWriter()

    val output: String
        get() = out.output

    fun writeApi() {
        out.writeLine("// Automatically generated from server-side definitions by Apina (https://apina.evident.fi)")
        out.writeLine("import Foundation")
        out.writeLine()

        writeTypes()
    }

    private fun writeTypes() {
        check(settings.imports.isEmpty()) { "Imports are not yet supported for Swift" }
        check(settings.brandedPrimitiveTypes.isEmpty()) { "Branded primitive types are not yet supported for Swift" }

        if (api.typeAliases.isNotEmpty()) {
            for ((alias, target) in api.typeAliases)
                out.writeLine("typealias ${alias.name} = ${target.toSwift()};")

            out.writeLine()
        }

        if (api.enumDefinitions.isNotEmpty()) {
            for (enum in api.enumDefinitions) {
                out.writeBlock("enum ${enum.type.name}: String, Codable") {
                    for (constant in enum.constants)
                        out.writeLine("case $constant")
                }
            }

            out.writeLine()
        }

        if (api.classDefinitions.isNotEmpty()) {
            for (classDefinition in api.classDefinitions) {
                out.writeStruct(classDefinition.type.name, ": Codable") {
                    for (property in classDefinition.properties) {
                        val nullable = property.type is ApiType.Nullable
                        val init = if (nullable) " = nil" else ""
                        out.writeLine("var ${property.name}: ${property.type.toSwift()}$init")
                    }
                }
            }

            out.writeLine()
        }

        for (union in api.discriminatedUnionDefinitions)
            writeDiscriminatedUnion(union)
    }

    private fun writeDiscriminatedUnion(definition: DiscriminatedUnionDefinition) {
        out.writeBlock("enum ${definition.type.name}") {
            for (classDef in definition.types.values) {
                val body = classDef.properties.joinToString(", ") { "${it.name}: ${it.type.toSwift()}" }
                out.writeLine("case ${classDef.type.name}($body)")
            }
        }

        out.writeLine()

        out.writeBlock("extension ${definition.type.name}: Codable") {
            val propertyNames = definition.types.values.flatMap { it.properties }.map { it.name }.toSortedSet()

            out.writeBlock("private enum CodingKeys: CodingKey") {
                out.writeLine("case ${definition.discriminator}, ${propertyNames.joinToString(", ")}")
            }

            out.writeBlock("private enum Discriminator: String, Codable") {
                out.writeLine("case ${definition.types.keys.joinToString(", ")}")
            }

            out.writeBlock("func encode(to encoder: Encoder) throws") {
                out.writeLine("var container = encoder.container(keyedBy: CodingKeys.self)")
                out.writeBlock("switch self") {
                    for ((key, member) in definition.types) {
                        out.writeDedentedLine("case let .${member.type.name}(${member.properties.joinToString(", ") { it.name }}):")
                        out.writeLine("try container.encode(Discriminator.$key, forKey: .${definition.discriminator})")
                        for (property in member.properties)
                            out.writeLine("try container.encode(${property.name}, forKey: .${property.name})")
                    }
                }
            }

            out.writeBlock("init(from decoder: Decoder) throws") {
                out.writeLine("let container = try decoder.container(keyedBy: CodingKeys.self)")
                out.writeLine("let type = try container.decode(Discriminator.self, forKey: .${definition.discriminator})")
                out.writeBlock("switch type") {
                    for ((key, member) in definition.types) {
                        out.writeDedentedLine("case .$key:")
                        out.writeCall("self = .${member.type.name}", member.properties.map {
                            it.name to "try container.decode(${it.type.toSwift()}.self, forKey: .${it.name})"
                        })
                        out.writeLine()
                    }
                }
            }
        }
    }
}

internal fun ApiType.toSwift(): String = when (this) {
    is ApiType.Array -> "[${elementType.toSwift()}]"
    is ApiType.BlackBox -> name.name
    is ApiType.Class -> name.name
    is ApiType.Dictionary -> "[String: ${valueType.toSwift()}]"
    is ApiType.Nullable -> type.toSwift() + "?"
    is ApiType.Primitive -> toSwift()
}

private fun ApiType.Primitive.toSwift(): String = when (this) {
    ApiType.Primitive.ANY -> "Any"
    ApiType.Primitive.STRING -> "String"
    ApiType.Primitive.BOOLEAN -> "Bool"
    ApiType.Primitive.INTEGER -> "Int"
    ApiType.Primitive.FLOAT -> "Float"
    ApiType.Primitive.VOID -> "Void"
}
