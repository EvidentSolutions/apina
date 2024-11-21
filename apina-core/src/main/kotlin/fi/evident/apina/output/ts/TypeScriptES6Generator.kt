package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.utils.readResourceAsString

class TypeScriptES6Generator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(
    api = api,
    settings = settings,
    resultFunctor = ResultFunctor.PROMISE,
    classDecorator = "",
    platformRuntimeCodePath = "typescript/runtime-es6.ts"
)
