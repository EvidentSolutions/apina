package fi.evident.apina.java.model

import org.objectweb.asm.Opcodes

enum class JavaVisibility {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    override fun toString() = name.toLowerCase()

    companion object {

        fun fromAccessFlags(access: Int): JavaVisibility = when {
            access and Opcodes.ACC_PUBLIC != 0 ->
                JavaVisibility.PUBLIC
            access and Opcodes.ACC_PROTECTED != 0 ->
                JavaVisibility.PROTECTED
            access and Opcodes.ACC_PRIVATE != 0 ->
                JavaVisibility.PRIVATE
            else ->
                JavaVisibility.PACKAGE
        }
    }
}
