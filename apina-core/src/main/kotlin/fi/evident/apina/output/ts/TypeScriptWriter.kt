package fi.evident.apina.output.ts

import fi.evident.apina.output.common.CodeWriter

/**
 * Helper for generating TypeScript code. Keeps track of indentation level
 * and supports proper writing of literal values.
 */
class TypeScriptWriter : CodeWriter<TypeScriptWriter>() {

    override val self: TypeScriptWriter
        get() = this

    fun writeExportedInterface(name: String, bodyWriter: () -> Unit) {
        writeBlock("export interface $name", bodyWriter)
    }

    fun writeImport(module: String, types: Collection<String>) {
        writeLine("import { ${types.joinToString(", ")} } from '$module';")
    }

    fun writeValue(obj: Any?): TypeScriptWriter {
        when (obj) {
            is Number ->
                write(obj.toString())
            is String ->
                writeString(obj)
            is Collection<*> ->
                writeCollection(obj)
            is Map<*, *> ->
                writeMap(obj)
            else ->
                write(obj.toString())
        }

        return this
    }

    private fun writeMap(obj: Map<*, *>) {
        if (obj.isEmpty()) {
            write("{}")
            return
        }

        writeBlock {
            val it = obj.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                writeValue(entry.key)
                write(": ")
                writeValue(entry.value)

                if (it.hasNext())
                    write(",")

                writeLine()
            }
        }
    }

    private fun writeCollection(obj: Collection<*>) {
        write("[")

        val it = obj.iterator()
        while (it.hasNext()) {
            writeValue(it.next())

            if (it.hasNext())
                write(", ")
        }

        write("]")
    }
}
