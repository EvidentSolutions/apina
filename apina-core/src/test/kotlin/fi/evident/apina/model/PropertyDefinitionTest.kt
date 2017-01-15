package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import org.junit.Test
import kotlin.test.assertEquals

class PropertyDefinitionTest {

    @Test
    fun toStringRepresentation() {
        assertEquals("string foo", PropertyDefinition("foo", ApiType.Primitive.STRING).toString())
    }
}
