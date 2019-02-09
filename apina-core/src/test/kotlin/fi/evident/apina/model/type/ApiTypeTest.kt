package fi.evident.apina.model.type

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApiTypeTest {

    @Test
    fun representations() {
        assertEquals("string", ApiType.Primitive.STRING.typeRepresentation())
        assertEquals("string[]", ApiType.Array(ApiType.Primitive.STRING).typeRepresentation())
        assertEquals("string[][]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).typeRepresentation())
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).typeRepresentation())
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).typeRepresentation())
        assertEquals("Dictionary<string>", ApiType.Dictionary(ApiType.Primitive.STRING).typeRepresentation())
        assertEquals("string | null", ApiType.Nullable(ApiType.Primitive.STRING).typeRepresentation())
    }
}
