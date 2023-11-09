package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.settings.TranslationSettings

/**
 * Generates Angular TypeScript code for client side.
 */
class TypeScriptAngularGenerator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(
    api = api,
    settings = settings,
    resultFunctor = "Observable",
    classDecorator = "@Injectable({providedIn: 'root'})",
    platformRuntimeCodePath = "typescript/runtime-angular.ts",
    platformSpecificImports = mapOf(
        "@angular/core" to listOf("Injectable", "NgModule"),
        "@angular/common/http" to listOf("HttpClient", "HttpClientModule", "HttpParams"),
        "rxjs" to listOf("Observable"),
        "rxjs/operators" to listOf("map"),
    )
)
