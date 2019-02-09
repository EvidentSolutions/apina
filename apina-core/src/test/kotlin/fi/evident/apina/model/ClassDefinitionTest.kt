package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassDefinitionTest {

    private val classDefinition = ClassDefinition(ApiTypeName("foo.Bar"))

    @Test
    fun duplicatePropertiesAreDisallowed() {

        classDefinition.addProperty(arbitraryProperty("foo"))
        assertThrows<IllegalArgumentException> {
            classDefinition.addProperty(arbitraryProperty("foo"))
        }
    }

    @Test
    fun hasProperty() {
        assertFalse(classDefinition.hasProperty("foo"))
        assertFalse(classDefinition.hasProperty("bar"))

        classDefinition.addProperty(arbitraryProperty("foo"))

        assertTrue(classDefinition.hasProperty("foo"))
        assertFalse(classDefinition.hasProperty("bar"))
    }

    private fun arbitraryProperty(name: String) = PropertyDefinition(name, ApiType.Primitive.STRING)
}
