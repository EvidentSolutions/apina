package fi.evident.apina.java.model.type

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavaTypeTest {

    @Test
    fun `primitive numbers`() {
        assertTrue(JavaType.Basic(Int::class.javaPrimitiveType!!).isPrimitiveNumber)
        assertTrue(JavaType.Basic(Long::class.javaPrimitiveType!!).isPrimitiveNumber)
        assertTrue(JavaType.Basic(Short::class.javaPrimitiveType!!).isPrimitiveNumber)
        assertTrue(JavaType.Basic(Float::class.javaPrimitiveType!!).isPrimitiveNumber)
        assertTrue(JavaType.Basic(Double::class.javaPrimitiveType!!).isPrimitiveNumber)

        assertFalse(JavaType.Basic(Boolean::class.javaPrimitiveType!!).isPrimitiveNumber)
        assertFalse(JavaType.Basic(Char::class.javaPrimitiveType!!).isPrimitiveNumber)
        assertFalse(JavaType.Basic(Int::class.javaObjectType).isPrimitiveNumber)
        assertFalse(JavaType.Basic(String::class.java).isPrimitiveNumber)
    }

    @Test
    fun `wide types`() {
        assertFalse(JavaType.Basic(Int::class.javaPrimitiveType!!).isWide)
        assertFalse(JavaType.Basic(Float::class.javaPrimitiveType!!).isWide)
        assertTrue(JavaType.Basic(Long::class.javaPrimitiveType!!).isWide)
        assertTrue(JavaType.Basic(Double::class.javaPrimitiveType!!).isWide)
    }

    @Test
    fun `package names`() {
        assertEquals("foo.bar", JavaType.Basic("foo.bar.Baz").packageName);
        assertEquals("foo.bar", JavaType.Basic("foo.bar.Baz\$Quux").packageName);
        assertEquals("", JavaType.Basic("int").packageName);
    }
}
