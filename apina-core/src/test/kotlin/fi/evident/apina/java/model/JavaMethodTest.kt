package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.junit.Test
import org.objectweb.asm.Opcodes
import java.util.Collections.emptyList
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavaMethodTest {

    @Test
    fun modifiers() {
        assertFalse(methodWithModifiers(0).isStatic)
        assertTrue(methodWithModifiers(Opcodes.ACC_STATIC).isStatic)
    }

    private fun methodWithModifiers(modifiers: Int) =
            JavaMethod(arbitraryClass(), "foo", JavaVisibility.PUBLIC, JavaType.basic<String>(), emptyList(), modifiers, TypeSchema())

    private fun arbitraryClass() =
            JavaClass(JavaType.Basic("test.Bar"), JavaType.basic<Any>(), emptyList(), 0, TypeSchema())
}
