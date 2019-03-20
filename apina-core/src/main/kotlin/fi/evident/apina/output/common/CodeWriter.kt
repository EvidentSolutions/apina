package fi.evident.apina.output.common

abstract class CodeWriter<Self : CodeWriter<Self>> {
    private val out = StringBuilder()
    private var indentationLevel = 0
    private var beginningOfLine = true

    val output: String
        get() = out.toString()

    protected abstract val self: Self

    private fun write(c: Char) {
        // Write indent if necessary, unless we are writing an empty line
        if (c != '\n' && beginningOfLine)
            out.append("    ".repeat(indentationLevel))

        out.append(c)

        beginningOfLine = c == '\n'
    }

    fun write(s: String): Self {
        for (c in s)
            write(c)

        return self
    }

    fun writeLine(s: String) {
        write(s)
        writeLine()
    }

    fun writeLine(): Self {
        write("\n")
        return self
    }

    fun writeBlock(block: () -> Unit) {
        writeLine("{")
        indent()

        block()

        dedent()
        write("}")
    }

    fun writeBlock(prefix: String, bodyWriter: () -> Unit) {
        write("$prefix ")
        writeBlock(bodyWriter)
        writeLine()
        writeLine()
    }

    fun writeValue(obj: Any?): Self {
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

        return self
    }

    protected open fun writeString(s: String) {
        write('\'')

        for (c in s) {
            when (c) {
                '\'' -> write("\\'")
                '\n' -> write("\\n")
                else -> write(c)
            }
        }

        write('\'')
    }

    protected abstract fun writeCollection(obj: Collection<*>)

    protected abstract fun writeMap(obj: Map<*, *>)

    private fun indent() {
        indentationLevel++
    }

    private fun dedent() {
        check(indentationLevel != 0) { "can't dedent with zero indentation" }

        indentationLevel--
    }
}
