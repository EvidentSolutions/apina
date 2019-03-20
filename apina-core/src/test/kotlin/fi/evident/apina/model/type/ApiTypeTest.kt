package fi.evident.apina.model.type

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApiTypeTest {

    @Test
    fun `typescript representations`() {
        assertEquals("string", ApiType.Primitive.STRING.toTypeScript())
        assertEquals("string[]", ApiType.Array(ApiType.Primitive.STRING).toTypeScript())
        assertEquals("string[][]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).toTypeScript())
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).toTypeScript())
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).toTypeScript())
        assertEquals("Dictionary<string>", ApiType.Dictionary(ApiType.Primitive.STRING).toTypeScript())
        assertEquals("string | null", ApiType.Nullable(ApiType.Primitive.STRING).toTypeScript())
    }
}
