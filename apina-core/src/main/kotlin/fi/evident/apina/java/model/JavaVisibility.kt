package fi.evident.apina.java.model

import org.objectweb.asm.Opcodes

enum class JavaVisibility {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    override fun toString() = name.lowercase()

    companion object {

        fun fromAccessFlags(access: Int): JavaVisibility = when {
            access and Opcodes.ACC_PUBLIC != 0 ->
                PUBLIC
            access and Opcodes.ACC_PROTECTED != 0 ->
                PROTECTED
            access and Opcodes.ACC_PRIVATE != 0 ->
                PRIVATE
            else ->
                PACKAGE
        }
    }
}
