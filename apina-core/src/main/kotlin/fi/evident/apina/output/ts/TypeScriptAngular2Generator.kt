package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.Endpoint
import fi.evident.apina.model.EndpointGroup
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.utils.readResourceAsString

/**
 * Generates Angular 2 TypeScript code for client side.
 */
class TypeScriptAngular2Generator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(api, settings, "", "", "Observable") {

    fun writeApi() {
        writeHeader()
        writeImports()
        writeTypes()
        writeRuntime()
        writeEndpoints(api.endpointGroups)
        writeModule()
    }

    private fun writeHeader() {
        // The project using our generated code might generate the code to directory where
        // it's checked by TSLint and we don't know what settings are used so just disable
        // TSLint for the whole file.
        out.writeLine("/* tslint:disable */")
    }

    val output: String
        get() = out.output

    private fun writeRuntime() {
        out.write(readResourceAsString("typescript/runtime-angular2.ts"))
        out.writeLine()
    }

    private fun writeImports() {
        val imports = settings.imports

        out.writeLine("import { Injectable, NgModule } from '@angular/core';")
        out.writeLine("import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';")
        out.writeLine("import { Observable } from 'rxjs';")
        out.writeLine("import { map } from 'rxjs/operators';")

        if (!imports.isEmpty()) {
            for (anImport in imports)
                out.writeLine("import { " + anImport.types.joinToString(", ") + " } from '" + anImport.moduleName + "';")

            out.writeLine()
        }
    }

    private fun writeEndpoints(endpointGroups: Collection<EndpointGroup>) {
        for (endpointGroup in endpointGroups) {
            out.writeLine("@Injectable()")
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

    private fun writeModule() {
        out.writeLine()

        out.writeLine("@NgModule({")
        out.writeLine("    imports: [HttpClientModule],")
        out.writeLine("    providers: [")

        for (endpointGroup in api.endpointGroups)
            out.writeLine("        " + endpointGroup.name + "Endpoint,")

        out.writeLine("        { provide: ApinaEndpointContext, useClass: DefaultApinaEndpointContext },")
        out.writeLine("        ApinaConfig")

        out.writeLine("    ]")
        out.writeLine("})")
        out.writeLine("export class ApinaModule {}")
    }
}
