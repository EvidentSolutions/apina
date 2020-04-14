package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavaFieldTest {

    @Test
    fun modifiers() {
        assertFalse(fieldWithModifiers(0).isStatic)
        assertFalse(fieldWithModifiers(0).isEnumConstant)
        assertTrue(fieldWithModifiers(Opcodes.ACC_STATIC).isStatic)
        assertTrue(fieldWithModifiers(Opcodes.ACC_ENUM).isEnumConstant)
        assertTrue(fieldWithModifiers(Opcodes.ACC_TRANSIENT).isTransient)
    }

    private fun fieldWithModifiers(modifiers: Int) =
        JavaField("foo", JavaVisibility.PUBLIC, JavaType.basic<String>(), modifiers)
}
