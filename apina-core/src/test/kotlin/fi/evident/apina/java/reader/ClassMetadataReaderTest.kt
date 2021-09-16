package fi.evident.apina.java.reader

import fi.evident.apina.java.reader.JavaTypeMatchers.basicType
import fi.evident.apina.java.reader.JavaTypeMatchers.singletonSchema
import fi.evident.apina.java.reader.JavaTypeMatchers.typeVariable
import fi.evident.apina.java.reader.JavaTypeMatchers.typeWithRepresentation
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("UNUSED_PARAMETER")
class ClassMetadataReaderTest {

    @Test
    fun loadingFields() {
        val javaClass = loadClass(TestClass::class.java)

        val fields = javaClass.fields

        assertThat(javaClass.schema, `is`(singletonSchema("T", basicType(CharSequence::class.java))))

        assertEquals(3, fields.size)
        assertThat(javaClass.getField("field1").type, `is`(typeWithRepresentation("java.lang.String")))
        assertThat(javaClass.getField("field2").type, `is`(typeWithRepresentation("java.util.List<java.lang.String>")))
        assertThat(javaClass.getField("field3").type, `is`(typeVariable("T")))
    }

    @Test
    fun innerClassWithOuterBounds() {
        loadClass(AnonymousInnerClassWithOuterBounds.createInnerClassInstance<Any>().javaClass)
    }

    @Test
    fun enumClasses() {
        val javaClass = loadClass(TestEnum::class.java)

        assertTrue(javaClass.isEnum)
        assertEquals(listOf("FOO", "BAR", "BAZ"), javaClass.enumConstants)
    }

    @Test
    fun `kotlin metadata`() {
        val metadata = loadClass(TestKotlinClass::class.java).kotlinMetadata
        assertNotNull(metadata)

        assertEquals(setOf("foo", "bar"), metadata.properties.map { it.name }.toSet())
    }

    @Suppress("unused")
    private class TestKotlinClass {
        val foo = ""
        val bar = ""
    }

    @Suppress("unused")
    private enum class TestEnum {
        FOO, BAR, BAZ;

        var instanceField: String? = null

        companion object {
            var staticField: String? = null
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private class TestClass<T : CharSequence> {

        var field1: String? = null

        var field2: List<String>? = null

        private val field3: T? = null

        fun method1() {}

        fun method2(): String = throw UnsupportedOperationException()

        fun method3(x: T): T = throw UnsupportedOperationException()
    }

    private object AnonymousInnerClassWithOuterBounds {
        fun <T> createInnerClassInstance(): Comparator<T> = Comparator { _, _ -> 0 }
    }
}
