package fi.evident.apina.model.settings

import fi.evident.apina.model.type.ApiTypeName
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TranslationSettingsTest {

    val settings = TranslationSettings()

    @Test
    fun addImports() {
        settings.addImport("mod1", listOf("Foo", "Bar"))
        settings.addImport("mod2", listOf("Baz"))

        settings.imports.any { it.moduleName == "mod1" && it.types == listOf("Foo", "Bar") }
        settings.imports.any { it.moduleName == "mod2" && it.types == listOf("Baz") }

        for (type in listOf("Foo", "Bar", "Baz"))
            assertTrue(settings.isImported(ApiTypeName(type)))
    }

    @Test
    fun addImportsToExistingModule() {
        settings.addImport("mod1", listOf("Foo", "Bar"))
        settings.addImport("mod1", listOf("Baz"))

        settings.imports.any { it.moduleName == "mod1" && it.types == listOf("Foo", "Bar", "Baz") }

        for (type in listOf("Foo", "Bar", "Baz"))
            assertTrue(settings.isImported(ApiTypeName(type)))
    }

    @Test
    fun importTwice() {
        settings.addImport("mod1", listOf("Foo", "Bar"))

        assertFailsWith<IllegalArgumentException> {
            settings.addImport("mod1", listOf("Bar"))
        }
    }
}
