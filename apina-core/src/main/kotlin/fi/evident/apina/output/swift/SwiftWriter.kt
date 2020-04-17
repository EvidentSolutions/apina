package fi.evident.apina.output.swift

import fi.evident.apina.output.common.CodeWriter

class SwiftWriter : CodeWriter<SwiftWriter>() {

    override val self: SwiftWriter
        get() = this

    fun writeStruct(name: String, suffix: String = "", bodyWriter: () -> Unit) {
        writeBlock("struct $name$suffix", bodyWriter)
    }

    fun writeCall(func: String, params: List<Pair<String, String>>) {
        writeLine("$func(")
        for ((i, param) in params.withIndex()) {
            val (name, expr) = param
            val suffix = if (i == params.lastIndex) "" else ","
            writeLine("        $name: $expr$suffix")
        }

        write(")")
    }
}
