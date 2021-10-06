package fi.evident.apina.model.settings

import fi.evident.apina.model.type.ApiTypeName
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TranslationSettingsTest {

    private val settings = TranslationSettings()

    @Test
    fun `add imports`() {
        settings.addImport("mod1", listOf("Foo", "Bar"))
        settings.addImport("mod2", listOf("Baz"))

        settings.imports.any { it.moduleName == "mod1" && it.types == listOf("Foo", "Bar") }
        settings.imports.any { it.moduleName == "mod2" && it.types == listOf("Baz") }

        for (type in listOf("Foo", "Bar", "Baz"))
            assertTrue(settings.isImported(ApiTypeName(type)))
    }

    @Test
    fun `add imports to existing module`() {
        settings.addImport("mod1", listOf("Foo", "Bar"))
        settings.addImport("mod1", listOf("Baz"))

        settings.imports.any { it.moduleName == "mod1" && it.types == listOf("Foo", "Bar", "Baz") }

        for (type in listOf("Foo", "Bar", "Baz"))
            assertTrue(settings.isImported(ApiTypeName(type)))
    }

    @Test
    fun `import twice`() {
        settings.addImport("mod1", listOf("Foo", "Bar"))

        assertFailsWith<IllegalArgumentException> {
            settings.addImport("mod1", listOf("Bar"))
        }
    }
}
