package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.output.ts.ResultFunctor.OBSERVABLE
import fi.evident.apina.output.ts.ResultFunctor.PROMISE

/**
 * Generates Angular TypeScript code for client side.
 */
class TypeScriptAngularGenerator(api: ApiDefinition, settings: TranslationSettings, resultFunctor: ResultFunctor) :
    AbstractTypeScriptGenerator(
        api = api,
        settings = settings,
        resultFunctor = resultFunctor,
        classDecorator = "@Injectable({providedIn: 'root'})",
        platformRuntimeCodePath = when (resultFunctor) {
            OBSERVABLE -> "typescript/runtime-angular.ts"
            PROMISE -> "typescript/runtime-angular-promise.ts"
        },
        platformSpecificImports = mapOf(
            "@angular/core" to listOf("Injectable", "Provider", "Type"),
            "@angular/common/http" to listOf("HttpClient", "HttpParams"),
        ) + resultFunctor.functorImports()
    )

private fun ResultFunctor.functorImports() = when (this) {
    PROMISE -> mapOf(
        "rxjs" to listOf("firstValueFrom"),
    )

    OBSERVABLE -> mapOf(
        "rxjs" to listOf("Observable"),
        "rxjs/operators" to listOf("map"),
    )
}
