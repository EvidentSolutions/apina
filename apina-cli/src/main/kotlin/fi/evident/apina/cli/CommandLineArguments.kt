package fi.evident.apina.cli

import fi.evident.apina.model.settings.BrandedPrimitiveType
import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.settings.Platform
import fi.evident.apina.model.settings.TypeWriteMode
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName

internal class CommandLineArguments {

    val files = mutableListOf<String>()
    val blackBoxPatterns = mutableListOf<String>()
    val controllerPatterns = mutableListOf<String>()
    val endpointUrlMethods = mutableListOf<String>()
    val imports = mutableListOf<ImportArgument>()
    val brandedPrimitiveTypes = mutableListOf<BrandedPrimitiveType>()
    var platform = Platform.ANGULAR
    var typeWriteMode = TypeWriteMode.INTERFACE
    var optionalTypeMode = OptionalTypeMode.NULL

    private fun parse(arg: String) {
        // This could be more general, but this is all we need for now.

        val blackBox = parseOptionalWithValue("black-box", arg)
        if (blackBox != null) {
            blackBoxPatterns.add(blackBox)
            return
        }

        val controller = parseOptionalWithValue("controller", arg)
        if (controller != null) {
            controllerPatterns.add(controller)
            return
        }

        val urlMethod = parseOptionalWithValue("url-method", arg)
        if (urlMethod != null) {
            endpointUrlMethods.add(urlMethod)
            return
        }

        val platform = parseOptionalWithValue("platform", arg)
        if (platform != null) {
            this.platform = Platform.valueOf(platform.uppercase())
            return
        }

        val typeWriteMode = parseOptionalWithValue("type-write-mode", arg)
        if (typeWriteMode != null) {
            this.typeWriteMode = TypeWriteMode.valueOf(typeWriteMode.uppercase())
            return
        }

        val optionalTypeMode = parseOptionalWithValue("optional-type-mode", arg)
        if (optionalTypeMode != null) {
            this.optionalTypeMode = OptionalTypeMode.valueOf(optionalTypeMode.uppercase())
            return
        }

        val anImport = parseOptionalWithValue("import", arg)
        if (anImport != null) {
            val colonIndex = anImport.indexOf(':')
            if (colonIndex == -1)
                throw IllegalArgumentException("invalid import: $anImport")

            val types = anImport.substring(0, colonIndex).split(",".toRegex()).toTypedArray()
            val module = anImport.substring(colonIndex + 1)

            imports.add(ImportArgument(types.asList(), module))
            return
        }

        val brandedPrimitiveType = parseOptionalWithValue("branded-primitive-types", arg)
        if (brandedPrimitiveType != null) {
            val definitions = brandedPrimitiveType.split(",")
            for (definition in definitions) {
                val values = definition.split(":", limit = 2)
                if (values.size != 2)
                    throw IllegalArgumentException("invalid branded primitive type: $definition")

                brandedPrimitiveTypes += BrandedPrimitiveType(
                    brandedType = ApiTypeName(values[0]),
                    implementationType = ApiType.Primitive.forName(values[1])
                )
            }
            return
        }

        if (arg.startsWith("--"))
            throw IllegalArgumentException("unknown argument $arg")

        files.add(arg)
    }

    class ImportArgument(val types: List<String>, val module: String)

    companion object {

        private fun parseOptionalWithValue(name: String, arg: String): String? {
            val prefix = "--$name="
            return if (arg.startsWith(prefix))
                arg.substring(prefix.length)
            else
                null
        }

        fun parse(args: Array<String>): CommandLineArguments {
            val result = CommandLineArguments()

            for (arg in args)
                result.parse(arg)

            return result
        }
    }
}
