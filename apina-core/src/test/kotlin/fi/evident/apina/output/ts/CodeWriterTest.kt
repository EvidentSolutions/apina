package fi.evident.apina.output.ts

import org.junit.Test
import java.util.*
import java.util.Collections.emptyMap
import kotlin.test.assertEquals

class CodeWriterTest {

    private val writer = CodeWriter()

    @Test
    fun indents() {
        writer.writeLine("foo ")
        writer.writeBlock {
            writer.writeLine("bar")
            writer.writeLine("baz")
        }
        writer.writeLine()

        assertEquals("foo \n{\n    bar\n    baz\n}\n", writer.output)
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
        val map = LinkedHashMap<String, Int>()
        map.put("foo", 1)
        map.put("bar", 2)
        map.put("baz", 3)
        writer.writeValue(map)

        assertEquals("{\n    'foo': 1,\n    'bar': 2,\n    'baz': 3\n}", writer.output)
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
