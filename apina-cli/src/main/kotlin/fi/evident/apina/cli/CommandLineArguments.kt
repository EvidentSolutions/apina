package fi.evident.apina.cli

import fi.evident.apina.model.settings.Platform
import fi.evident.apina.model.settings.TypeWriteMode
import java.util.*

internal class CommandLineArguments {

    val files = mutableListOf<String>()
    val blackBoxPatterns = mutableListOf<String>()
    val controllerPatterns = mutableListOf<String>()
    val imports = mutableListOf<ImportArgument>()
    var platform = Platform.ANGULAR
    var typeWriteMode = TypeWriteMode.CLASS

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

        val platform = parseOptionalWithValue("platform", arg)
        if (platform != null) {
            this.platform = Platform.valueOf(platform.toUpperCase())
            return
        }

        val typeWriteMode = parseOptionalWithValue("type-write-mode", arg)
        if (typeWriteMode != null) {
            this.typeWriteMode = TypeWriteMode.valueOf(typeWriteMode.toUpperCase())
            return
        }

        val anImport = parseOptionalWithValue("import", arg)
        if (anImport != null) {
            val colonIndex = anImport.indexOf(':')
            if (colonIndex == -1)
                throw IllegalArgumentException("invalid import: $anImport")

            val types = anImport.substring(0, colonIndex).split(",".toRegex()).toTypedArray()
            val module = anImport.substring(colonIndex + 1)

            imports.add(ImportArgument(Arrays.asList(*types), module))
            return
        }

        if (arg.startsWith("--"))
            throw java.lang.IllegalArgumentException("unknown argument $arg")

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
