package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.Endpoint
import fi.evident.apina.model.EndpointGroup
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.utils.ResourceUtils.readResourceAsString
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Generates Angular 1 TypeScript code for client side.
 */
class TypeScriptAngular1Generator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(api, settings, "Types.", "Support.", "Support.IPromise") {

    @Throws(IOException::class)
    fun writeApi() {
        writeHeader()
        writeImports()
        out.writeExportedNamespace("Types") { this.writeTypes() }
        writeEndpoints(api.endpointGroups)
        writeRuntime()
    }

    private fun writeHeader() {
        // The project using our generated code might generate the code to directory where
        // it's checked by TSLint and we don't know what settings are used so just disable
        // TSLint for the whole file.
        out.writeLine("/* tslint:disable */")
    }

    private fun writeCreateEndpointGroups() {
        out.write("export function createEndpointGroups(context: Support.EndpointContext): Endpoints.IEndpointGroups ").writeBlock {
            out.write("return ").writeBlock {
                val it = api.endpointGroups.iterator()
                while (it.hasNext()) {
                    val endpointGroup = it.next()

                    out.write(String.format("%s: new Endpoints.%s(context)", endpointGroup.name.decapitalize(), endpointGroup.name))

                    if (it.hasNext())
                        out.write(",")

                    out.writeLine()
                }
            }

            out.writeLine(";")
        }

        out.writeLine()
        out.writeLine()
    }

    val output: String
        get() = out.output

    @Throws(IOException::class)
    private fun writeRuntime() {
        out.write(readResourceAsString("typescript/runtime-angular1.ts", UTF_8))
        out.writeLine()
    }

    private fun writeImports() {
        val imports = settings.imports

        if (!imports.isEmpty()) {
            for (anImport in imports)
                out.writeLine("import { " + anImport.types.joinToString(", ") + " } from '" + anImport.moduleName + "';")

            out.writeLine()
        }
    }

    private fun writeEndpoints(endpointGroups: Collection<EndpointGroup>) {
        out.writeExportedNamespace("Endpoints") {

            val names = endpointGroups.map { it.name.decapitalize() }
            out.write("export const endpointGroupNames = ").writeValue(names).writeLine(";").writeLine()

            for (endpointGroup in endpointGroups) {
                out.writeBlock("export class " + endpointGroup.name) {

                    out.write("static KEY = ").writeValue(endpointGroup.name.decapitalize() + "Endpoints").writeLine(";").writeLine()

                    out.writeBlock("constructor(private context: Support.EndpointContext)") { }

                    for (endpoint in endpointGroup.endpoints) {
                        writeEndpoint(endpoint)
                        out.writeLine().writeLine()
                    }
                }
            }

            out.writeExportedInterface("IEndpointGroups") {
                for (endpointGroup in endpointGroups)
                    out.writeLine(endpointGroup.name.decapitalize() + ": " + endpointGroup.name)
            }

            writeCreateEndpointGroups()
        }
    }

    private fun writeEndpoint(endpoint: Endpoint) {
        out.write(endpointSignature(endpoint)).write(" ").writeBlock { out.write("return this.context.request(").writeValue(AbstractTypeScriptGenerator.createConfig(endpoint)).writeLine(");") }
    }


}
