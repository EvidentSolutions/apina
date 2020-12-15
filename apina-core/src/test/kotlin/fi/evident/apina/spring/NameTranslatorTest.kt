package fi.evident.apina.spring

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NameTranslatorTest {

    private val translator = NameTranslator()

    @Test
    fun translatingNamesUnqualified() {
        assertEquals("Foo", translator.translateClassName("Foo"))
    }

    @Test
    fun translatingQualifiedNames() {
        assertEquals("Baz", translator.translateClassName("foo.bar.Baz"))
    }

    @Test
    fun translatingInnerClassNames() {
        assertEquals("Quux", translator.translateClassName("foo.bar.Baz\$Quux"))
    }

    @Test
    fun removeControllerSuffixFromEndpointNames() {
        assertEquals("Bar", translator.translateEndpointGroupName("foo.Bar"))
        assertEquals("Bar", translator.translateEndpointGroupName("foo.BarController"))
    }
}
