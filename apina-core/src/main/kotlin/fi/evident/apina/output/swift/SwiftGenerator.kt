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
    }
}
