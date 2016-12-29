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

    fun write(s: String): CodeWriter {
        if (!s.isEmpty()) {
            writeIndentIfAtBegin()
            out.append(s)
        }

        return this
    }

    private fun writeIndentIfAtBegin() {
        if (beginningOfLine) {
            writeIndent()
            beginningOfLine = false
        }
    }

    fun writeLine(): CodeWriter {
        out.append('\n')
        beginningOfLine = true
        return this
    }

    fun writeValue(obj: Any?): CodeWriter {
        writeIndentIfAtBegin()

        if (obj is Number) {
            out.append(obj.toString())
        } else if (obj is Map<*, *>) {
            writeMap(obj)
        } else if (obj is String) {
            writeString(obj)
        } else if (obj is Collection<*>) {
            writeCollection(obj)
        } else {
            out.append(obj.toString())
        }

        return this
    }

    fun writeExportedNamespace(name: String, bodyWriter: () -> Unit): CodeWriter {
        return writeBlock("export namespace " + name, bodyWriter)
    }

    fun writeExportedInterface(name: String, bodyWriter: () -> Unit): CodeWriter {
        return writeBlock("export interface " + name, bodyWriter)
    }

    fun writeExportedClass(name: String, bodyWriter: () -> Unit): CodeWriter {
        return writeBlock("export class " + name, bodyWriter)
    }

    fun writeBlock(prefix: String, bodyWriter: () -> Unit): CodeWriter {
        return write(prefix + " ").writeBlock(bodyWriter).writeLine().writeLine()
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

        writeBlock({
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
        })
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
        out.append('\'')
        var i = 0
        val len = s.length
        while (i < len) {
            val c = s[i]
            when (c) {
                '\'' -> out.append("\\'")
                '\n' -> out.append("\\n")
                else -> out.append(c)
            }
            i++
        }
        out.append('\'')
    }

    fun indent(): CodeWriter {
        indentationLevel++
        return this
    }

    fun dedent(): CodeWriter {
        if (indentationLevel == 0) throw IllegalStateException()

        indentationLevel--
        return this
    }

    private fun writeIndent() {
        for (i in 0..indentationLevel - 1)
            out.append("    ")
    }
}
