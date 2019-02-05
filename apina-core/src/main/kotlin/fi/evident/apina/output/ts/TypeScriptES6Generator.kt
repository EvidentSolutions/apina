package fi.evident.apina.output.ts

import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.utils.readResourceAsString

class TypeScriptES6Generator(api: ApiDefinition, settings: TranslationSettings) : AbstractTypeScriptGenerator(api, settings, "", "", "Promise", "") {

    val output: String
        get() = out.output

    override fun writeRuntime() {
        out.write(readResourceAsString("typescript/runtime-es6.ts"))
        out.writeLine()
    }
}
