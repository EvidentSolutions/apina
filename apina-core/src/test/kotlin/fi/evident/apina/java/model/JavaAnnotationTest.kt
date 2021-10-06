package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType.Basic
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList
import kotlin.test.assertEquals

class JavaAnnotationTest {

    @Test
    fun `toString without parameters`() {
        val annotation = newAnnotation("foo.bar.Baz")
        assertEquals("@foo.bar.Baz", annotation.toString())
    }

    @Test
    fun `toString with just value`() {
        val annotation = newAnnotation("foo.bar.Baz")
        annotation.setAttribute("value", 42)

        assertEquals("@foo.bar.Baz(42)", annotation.toString())
    }

    @Test
    fun `unwrap array arguments if necessary`() {
        val annotation = newAnnotation("foo.bar.Baz")
        annotation.setAttribute("value", arrayOf<Any>("foo"))

        assertEquals("foo", annotation.getAttribute<String>("value"))
    }

    @Test
    fun toStringWithNonValueAttribute() {
        val annotation = newAnnotation("foo.bar.Baz")
        annotation.setAttribute("foo", 42)

        assertEquals("@foo.bar.Baz(foo=42)", annotation.toString())
    }

    @Test
    fun `toString with multiple attributes`() {
        val annotation = newAnnotation("foo.bar.Baz")
        annotation.setAttribute("value", 1)
        annotation.setAttribute("foo", 2)
        annotation.setAttribute("bar", 3)

        assertEquals("@foo.bar.Baz(value=1, foo=2, bar=3)", annotation.toString())
    }

    @Test
    fun `array values`() {
        val annotation = newAnnotation("foo.bar.Baz")
        annotation.setAttribute("array", arrayOf<Any>(1, 2, 3))
        annotation.setAttribute("single", 1)

        assertEquals(listOf(1, 2, 3), annotation.getAttributeValues("array"))
        assertEquals(listOf(1), annotation.getAttributeValues("single"))
        assertEquals(emptyList(), annotation.getAttributeValues("nonexistent"))
    }

    private fun newAnnotation(name: String) = JavaAnnotation(Basic(name))
}
