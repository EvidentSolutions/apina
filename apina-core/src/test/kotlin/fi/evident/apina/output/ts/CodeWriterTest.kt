package fi.evident.apina.output.ts

import org.junit.jupiter.api.Test
import java.util.Collections.emptyMap
import kotlin.test.assertEquals

class CodeWriterTest {

    private val writer = CodeWriter()

    @Test
    fun indents() {
        writer.writeLine("foo")
        writer.writeBlock {
            writer.writeLine("bar")
            writer.writeLine("baz")
        }

        assertEquals("""
            foo
            {
                bar
                baz
            }
        """.trimIndent(), writer.output)
    }

    @Test
    fun `indents are properly handled when text has line breaks`() {
        writer.writeLine("foo")
        writer.writeBlock {
            writer.writeLine("""
                bar
                baz
            """.trimIndent())
        }

        assertEquals("""
            foo
            {
                bar
                baz
            }
        """.trimIndent(), writer.output)
    }

    @Test
    fun stringValues() {
        writer.writeValue("foo 'bar' baz\nquux")

        assertEquals("'foo \\'bar\\' baz\\nquux'", writer.output)
    }

    @Test
    fun numberValues() {
        writer.writeValue(42)

        assertEquals("42", writer.output)
    }

    @Test
    fun booleanValues() {
        writer.writeValue(false)

        assertEquals("false", writer.output)
    }

    @Test
    fun mapValues() {
        writer.writeValue(mapOf(
                "foo" to 1,
                "bar" to 2,
                "baz" to 3))

        assertEquals("""
            {
                'foo': 1,
                'bar': 2,
                'baz': 3
            }
        """.trimIndent(), writer.output)
    }

    @Test
    fun emptyMapValues() {
        writer.writeValue(emptyMap<Any, Any>())

        assertEquals("{}", writer.output)
    }

    @Test
    fun collectionValues() {
        writer.writeValue(listOf(1, 2, 3))

        assertEquals("[1, 2, 3]", writer.output)
    }
}
