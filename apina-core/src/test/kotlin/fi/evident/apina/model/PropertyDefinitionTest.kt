package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropertyDefinitionTest {

    @Test
    fun `toString representation`() {
        assertEquals("string foo", PropertyDefinition("foo", ApiType.Primitive.STRING).toString())
    }
}
