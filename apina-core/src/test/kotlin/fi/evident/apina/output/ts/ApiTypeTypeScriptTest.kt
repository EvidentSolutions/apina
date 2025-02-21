package fi.evident.apina.output.ts

import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApiTypeTypeScriptTest {

    @Test
    fun `TypeScript representations`() {
        assertEquals("string", ApiType.Primitive.STRING.toTypeScript(OptionalTypeMode.NULL))
        assertEquals("string[]", ApiType.Array(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("string[][]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Record<string, string>", ApiType.Dictionary(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Record<string, string | null>", ApiType.Dictionary(ApiType.Nullable(ApiType.Primitive.STRING)).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("Record<string, string | undefined>", ApiType.Dictionary(ApiType.Nullable(ApiType.Primitive.STRING)).toTypeScript(OptionalTypeMode.UNDEFINED))
        assertEquals("string | null", ApiType.Nullable(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.NULL))
        assertEquals("string | undefined", ApiType.Nullable(ApiType.Primitive.STRING).toTypeScript(OptionalTypeMode.UNDEFINED))
    }
}
