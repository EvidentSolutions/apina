package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.junit.jupiter.api.Test
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
        JavaMethod(
            owningClass = arbitraryClass(),
            name = "foo",
            visibility = JavaVisibility.PUBLIC,
            returnType = JavaType.basic<String>(),
            parameters = emptyList(),
            modifiers = modifiers,
            schema = TypeSchema()
        )

    private fun arbitraryClass() =
        JavaClass(
            type = JavaType.Basic("test.Bar"),
            superClass = JavaType.basic<Any>(),
            interfaces = emptyList(),
            modifiers = 0,
            schema = TypeSchema()
        )
}
