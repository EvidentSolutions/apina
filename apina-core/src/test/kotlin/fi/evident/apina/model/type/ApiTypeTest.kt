package fi.evident.apina.model.type

import fi.evident.apina.model.settings.OptionalTypeMode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApiTypeTest {

    @Test
    fun `TypeScript representations`() {
        assertEquals("string", ApiType.Primitive.STRING.toTypeScript(OptionalTypeMode.NULL))
        assertEquals("string[]", ApiType.Array(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("string[][]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Dictionary<string>", ApiType.Dictionary(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Dictionary<string | null>", ApiType.Dictionary(ApiType.Nullable(ApiType.Primitive.STRING)).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Dictionary<string | undefined>", ApiType.Dictionary(ApiType.Nullable(ApiType.Primitive.STRING)).toTypeScript(OptionalTypeMode.UNDEFINED))
        assertEquals("string | null", ApiType.Nullable(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("string | undefined", ApiType.Nullable(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.UNDEFINED))
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
