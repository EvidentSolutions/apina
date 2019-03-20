package fi.evident.apina.model.type

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApiTypeTest {

    @Test
    fun `TypeScript representations`() {
        assertEquals("string", ApiType.Primitive.STRING.toTypeScript())
        assertEquals("string[]", ApiType.Array(ApiType.Primitive.STRING).toTypeScript())
        assertEquals("string[][]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).toTypeScript())
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).toTypeScript())
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).toTypeScript())
        assertEquals("Dictionary<string>", ApiType.Dictionary(ApiType.Primitive.STRING).toTypeScript())
        assertEquals("string | null", ApiType.Nullable(ApiType.Primitive.STRING).toTypeScript())
    }

    @Test
    fun `Swift representations`() {
        assertEquals("String", ApiType.Primitive.STRING.toSwift())
        assertEquals("[String]", ApiType.Array(ApiType.Primitive.STRING).toSwift())
        assertEquals("[[String]]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).toSwift())
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).toSwift())
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).toSwift())
        assertEquals("[String: String]", ApiType.Dictionary(ApiType.Primitive.STRING).toSwift())
        assertEquals("String?", ApiType.Nullable(ApiType.Primitive.STRING).toSwift())
    }
}
