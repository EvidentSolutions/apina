package fi.evident.apina.output.ts

import org.junit.jupiter.api.Test
import java.util.Collections.emptyMap
import kotlin.test.assertEquals

class TypeScriptWriterTest {

    private val writer = TypeScriptWriter()

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
    fun `string values`() {
        writer.writeValue("foo 'bar' baz\nquux")

        assertEquals("'foo \\'bar\\' baz\\nquux'", writer.output)
    }

    @Test
    fun `number values`() {
        writer.writeValue(42)

        assertEquals("42", writer.output)
    }

    @Test
    fun `boolean values`() {
        writer.writeValue(false)

        assertEquals("false", writer.output)
    }

    @Test
    fun `map values`() {
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
    fun `empty map values`() {
        writer.writeValue(emptyMap<Any, Any>())

        assertEquals("{}", writer.output)
    }

    @Test
    fun `collection values`() {
        writer.writeValue(listOf(1, 2, 3))

        assertEquals("[1, 2, 3]", writer.output)
    }

    @Test
    fun imports() {
        writer.writeImport("@angular/common/http", listOf("HttpClient", "HttpClientModule", "HttpParams"))

        assertEquals("import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';\n", writer.output)
    }
}
