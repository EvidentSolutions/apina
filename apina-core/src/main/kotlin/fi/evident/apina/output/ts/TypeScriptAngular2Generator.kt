package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.utils.readResourceAsString

/**
 * Generates Angular 2 TypeScript code for client side.
 */
class TypeScriptAngular2Generator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(api, settings, "Observable", "@Injectable()") {

    val output: String
        get() = out.output

    override fun writeRuntime() {
        out.write(readResourceAsString("typescript/runtime-angular2.ts"))
        out.writeLine()
    }

    override fun writePlatformSpecificImports() {
        out.writeLine("import { Injectable, NgModule } from '@angular/core';")
        out.writeLine("import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';")
        out.writeLine("import { Observable } from 'rxjs';")
        out.writeLine("import { map } from 'rxjs/operators';")
    }

    override fun writePlatformSpecific() {
        out.writeLine()

        out.writeLine("export function apinaConfigFactory() {")
        out.writeLine("    return new ApinaConfig();")
        out.writeLine("}")
        out.writeLine()

        out.writeLine("@NgModule({")
        out.writeLine("    imports: [HttpClientModule],")
        out.writeLine("    providers: [")

        for (endpointGroup in api.endpointGroups)
            out.writeLine("        " + endpointGroup.name + "Endpoint,")

        out.writeLine("        { provide: ApinaEndpointContext, useClass: DefaultApinaEndpointContext },")
        out.writeLine("        { provide: ApinaConfig, useFactory: apinaConfigFactory }")

        out.writeLine("    ]")
        out.writeLine("})")
        out.writeLine("export class ApinaModule {}")
    }
}
