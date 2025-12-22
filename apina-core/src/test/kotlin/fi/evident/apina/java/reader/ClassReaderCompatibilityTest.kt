package fi.evident.apina.java.reader

import org.objectweb.asm.ClassWriter
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.RETURN
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClassReaderCompatibilityTest {

    @Test
    fun `read Java 1 class file`() {
        checkReadingBytecodeVersion(45)
    }

    @Test
    fun `read Java 21 class file`() {
        checkReadingBytecodeVersion(65)
    }

    @Test
    fun `read Java 25 class file`() {
        checkReadingBytecodeVersion(69)
    }

    private fun checkReadingBytecodeVersion(bytecodeVersion: Int) {
        val classWriter = ClassWriter(0)
        classWriter.visit(bytecodeVersion, ACC_PUBLIC, "test/MyClass", null, "java/lang/Object", null)

        classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null).apply {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            visitInsn(RETURN)
            visitMaxs(1, 1)
            visitEnd()
        }

        classWriter.visitEnd()

        val classBytes = classWriter.toByteArray()

        val inputStream = ByteArrayInputStream(classBytes)
        val javaClass = ClassMetadataReader.loadMetadata(inputStream)

        assertNotNull(javaClass, "Should be able to read class file of version $bytecodeVersion")
        assertEquals("test.MyClass", javaClass.name)
    }
}
