package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.reader.loadClassesFromInheritanceTree
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.HTTPMethod
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import org.junit.Test
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SpringModelReaderTest {

    val settings = TranslationSettings()

    @Test
    fun parseSimpleController() {
        @Suppress("unused")
        @RestController
        class MyController {

            @PostMapping("/foo")
            fun foo(): String = error("dummy")

            @GetMapping("/bar")
            fun bar(): String = error("dummy")
        }

        val model = readModel<MyController>()

        assertEquals(1, model.endpointGroupCount, "endpointGroupCount")
        assertEquals(2, model.endpointCount, "endpointCount")

        val endpointGroup = model.endpointGroups.single()
        assertEquals("My", endpointGroup.name)

        val foo = assertNotNull(endpointGroup.endpoints.find { it.name == "foo" }, "foo")
        val bar = assertNotNull(endpointGroup.endpoints.find { it.name == "bar" }, "bar")
        assertEquals(HTTPMethod.POST, foo.method)
        assertEquals(HTTPMethod.GET, bar.method)
    }

    @Test
    @Suppress("unused")
    fun referenceToGenericClass() {
        abstract class Bar<A>
        abstract class Foo<B> : Bar<B>()

        @RestController
        class ControllerWithVariables : Foo<String>() {

            @GetMapping("/foo")
            fun foo(): Foo<String> = error("")
        }

        val model = readModel<ControllerWithVariables>()

        val endpoint = model.endpointGroups.single().endpoints.single()
        assertEquals(ApiType.Class("Foo"), assertNotNull(endpoint.responseBody))
    }

    private inline fun <reified T : Any> readModel(): ApiDefinition {
        val model = JavaModel()
        model.loadClassesFromInheritanceTree<T>()
        model.loadClassesFromInheritanceTree<PostMapping>()
        model.loadClassesFromInheritanceTree<GetMapping>()
        return SpringModelReader.readApiDefinition(model, settings)
    }
}

