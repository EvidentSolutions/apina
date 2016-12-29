package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassDefinitionTest {

    private val classDefinition = ClassDefinition(ApiTypeName("foo.Bar"))

    @Test(expected = IllegalArgumentException::class)
    fun duplicatePropertiesAreDisallowed() {
        classDefinition.addProperty(arbitraryProperty("foo"))
        classDefinition.addProperty(arbitraryProperty("foo"))
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
