package fi.evident.apina.spring

import org.junit.Test
import kotlin.test.assertEquals

class NameTranslatorTest {

    @Test
    fun translatingNamesUnqualified() {
        assertEquals("Foo", translateClassName("Foo"))
    }

    @Test
    fun translatingQualifiedNames() {
        assertEquals("Baz", translateClassName("foo.bar.Baz"))
    }

    @Test
    fun translatingInnerClassNames() {
        assertEquals("Quux", translateClassName("foo.bar.Baz\$Quux"))
    }

    @Test
    fun removeControllerSuffixFromEndpointNames() {
        assertEquals("Bar", translateEndpointGroupName("foo.Bar"))
        assertEquals("Bar", translateEndpointGroupName("foo.BarController"))
    }
}
