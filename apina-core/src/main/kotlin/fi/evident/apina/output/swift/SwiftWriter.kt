package fi.evident.apina.output.swift

import fi.evident.apina.output.common.CodeWriter

class SwiftWriter : CodeWriter<SwiftWriter>() {

    override val self: SwiftWriter
        get() = this

    fun writeStruct(name: String, suffix: String = "", bodyWriter: () -> Unit) {
        writeBlock("struct $name$suffix", bodyWriter)
    }
}
