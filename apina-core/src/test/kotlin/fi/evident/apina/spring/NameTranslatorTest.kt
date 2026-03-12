package fi.evident.apina.spring

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NameTranslatorTest {

    private val translator = NameTranslator()

    @Test
    fun translatingNamesUnqualified() {
        assertEquals("Foo", translator.translateClassName("Foo", qualifyNestedClasses = false))
    }

    @Test
    fun translatingQualifiedNames() {
        assertEquals("Baz", translator.translateClassName("foo.bar.Baz", qualifyNestedClasses = false))
    }

    @Test
    fun translatingInnerClassNames() {
        assertEquals("Quux", translator.translateClassName("foo.bar.Baz\$Quux", qualifyNestedClasses = false))
    }

    @Test
    fun removeControllerSuffixFromEndpointNames() {
        assertEquals("Bar", translator.translateEndpointGroupName("foo.Bar"))
        assertEquals("Bar", translator.translateEndpointGroupName("foo.BarController"))
    }

    @Test
    fun translatingNestedClassNamesWithoutQualification() {
        assertEquals("Bar", translator.translateClassName("com.example.Foo\$Bar", qualifyNestedClasses = false))
        assertEquals("Baz", translator.translateClassName("com.example.Foo\$Bar\$Baz", qualifyNestedClasses = false))
    }

    @Test
    fun translatingNestedClassNamesWithQualification() {
        assertEquals("Foo_Bar", translator.translateClassName("com.example.Foo\$Bar", qualifyNestedClasses = true))
        assertEquals("Foo_Bar_Baz", translator.translateClassName("com.example.Foo\$Bar\$Baz", qualifyNestedClasses = true))
    }

    @Test
    fun translatingNonNestedClassNamesWithQualification() {
        assertEquals("Foo", translator.translateClassName("com.example.Foo", qualifyNestedClasses = true))
    }
}
