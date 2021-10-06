package fi.evident.apina.model

import fi.evident.apina.model.type.ApiTypeName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnumDefinitionTest {

    @Test
    fun `toString representation`() {
        assertEquals("Foo", EnumDefinition(ApiTypeName("Foo"), listOf("BAR", "BAZ")).toString())
    }
}
