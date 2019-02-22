package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.utils.readResourceAsString

/**
 * Generates Angular TypeScript code for client side.
 */
class TypeScriptAngularGenerator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(api, settings, "Observable", "@Injectable()") {

    override fun writeRuntime() {
        out.write(readResourceAsString("typescript/runtime-angular.ts"))
        out.writeLine()
    }

    override fun writePlatformSpecificImports() {
        out.writeImport("@angular/core", listOf("Injectable", "NgModule"))
        out.writeImport("@angular/common/http", listOf("HttpClient", "HttpClientModule", "HttpParams"))
        out.writeImport("rxjs", listOf("Observable"))
        out.writeImport("rxjs/operators", listOf("map"))
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
            out.writeLine("        " + endpointClassName(endpointGroup) + ",")

        out.writeLine("        { provide: ApinaEndpointContext, useClass: DefaultApinaEndpointContext },")
        out.writeLine("        { provide: ApinaConfig, useFactory: apinaConfigFactory }")

        out.writeLine("    ]")
        out.writeLine("})")
        out.writeLine("export class ApinaModule {}")
    }
}
