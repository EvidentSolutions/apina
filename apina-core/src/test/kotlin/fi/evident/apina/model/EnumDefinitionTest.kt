package fi.evident.apina.model

import fi.evident.apina.model.type.ApiTypeName
import org.junit.Test
import kotlin.test.assertEquals

class EnumDefinitionTest {
    @Test
    fun toStringRepresentation() {
        assertEquals("Foo", EnumDefinition(ApiTypeName("Foo"), listOf("BAR", "BAZ")).toString())
    }
}
