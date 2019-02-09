package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import java.util.Collections.emptyList
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavaClassTest {

    @Test
    fun interfaces() {
        assertFalse(classWithModifiers(Modifier.PUBLIC).isInterface)
        assertTrue(classWithModifiers(Modifier.INTERFACE).isInterface)
        assertTrue(classWithModifiers(Modifier.INTERFACE or Modifier.PUBLIC).isInterface)
    }

    @Test
    fun annotations() {
        assertFalse(classWithModifiers(Modifier.INTERFACE).isAnnotation)
        assertTrue(classWithModifiersAndInterfaces(Modifier.INTERFACE, listOf(JavaType.basic<Annotation>())).isAnnotation)
    }

    private fun classWithModifiers(modifiers: Int) =
            classWithModifiersAndInterfaces(modifiers, emptyList())

    private fun classWithModifiersAndInterfaces(modifiers: Int, interfaces: List<JavaType>) =
            JavaClass(JavaType.Basic("test.Foo"), JavaType.Basic("test.Bar"), interfaces, modifiers, TypeSchema())
}
