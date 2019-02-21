package fi.evident.apina.output.ts

/**
 * Helper for generating TypeScript code. Keeps track of indentation level
 * and supports proper writing of literal values.
 */
internal class CodeWriter {

    private val out = StringBuilder()
    private var indentationLevel = 0
    private var beginningOfLine = true

    fun writeLine(s: String): CodeWriter {
        write(s)
        writeLine()
        return this
    }

    val output: String
        get() = out.toString()

    private fun write(c: Char): CodeWriter {
        // Write indent if necessary, unless we are writing an empty line
        if (c != '\n' && beginningOfLine)
            out.append("    ".repeat(indentationLevel))

        out.append(c)

        beginningOfLine = c == '\n'

        return this
    }

    fun write(s: String): CodeWriter {
        for (c in s)
            write(c)

        return this
    }

    fun writeLine(): CodeWriter {
        write("\n")
        return this
    }

    fun writeValue(obj: Any?): CodeWriter {
        when (obj) {
            is Number ->
                write(obj.toString())
            is Map<*, *> ->
                writeMap(obj)
            is String ->
                writeString(obj)
            is Collection<*> ->
                writeCollection(obj)
            else ->
                write(obj.toString())
        }

        return this
    }

    fun writeExportedInterface(name: String, bodyWriter: () -> Unit): CodeWriter {
        return writeBlock("export interface $name", bodyWriter)
    }

    fun writeExportedClass(name: String, bodyWriter: () -> Unit): CodeWriter {
        return writeBlock("export class $name", bodyWriter)
    }

    fun writeBlock(prefix: String, bodyWriter: () -> Unit): CodeWriter {
        return write("$prefix ").writeBlock(bodyWriter).writeLine().writeLine()
    }

    fun writeImport(module: String, types: Collection<String>): CodeWriter {
        return writeLine("import { ${types.joinToString(", ")} } from '$module';")
    }

    fun writeBlock(block: () -> Unit): CodeWriter {
        writeLine("{")
        indent()

        block()

        dedent()
        write("}")

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

    private fun writeString(s: String) {
        write('\'')
        var i = 0
        val len = s.length
        while (i < len) {
            val c = s[i]
            when (c) {
                '\'' -> write("\\'")
                '\n' -> write("\\n")
                else -> write(c)
            }
            i++
        }
        write('\'')
    }

    private fun indent(): CodeWriter {
        indentationLevel++
        return this
    }

    private fun dedent(): CodeWriter {
        if (indentationLevel == 0) throw IllegalStateException()

        indentationLevel--
        return this
    }
}
