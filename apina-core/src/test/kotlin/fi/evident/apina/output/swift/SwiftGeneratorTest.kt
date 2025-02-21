package fi.evident.apina.output.swift

import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SwiftGeneratorTest {

    @Test
    fun `Swift representations of API types`() {
        assertEquals("String", ApiType.Primitive.STRING.toSwift())
        assertEquals("[String]", ApiType.Array(ApiType.Primitive.STRING).toSwift())
        assertEquals("[[String]]", ApiType.Array(ApiType.Array(ApiType.Primitive.STRING)).toSwift())
        assertEquals("Foo", ApiType.BlackBox(ApiTypeName("Foo")).toSwift())
        assertEquals("Foo", ApiType.Class(ApiTypeName("Foo")).toSwift())
        assertEquals("[String: String]", ApiType.Dictionary(ApiType.Primitive.STRING).toSwift())
        assertEquals("String?", ApiType.Nullable(ApiType.Primitive.STRING).toSwift())
    }
}
