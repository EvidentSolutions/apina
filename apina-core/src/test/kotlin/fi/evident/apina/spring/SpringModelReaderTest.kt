package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.reader.loadClassesFromInheritanceTree
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.Endpoint
import fi.evident.apina.model.EndpointGroup
import fi.evident.apina.model.HTTPMethod
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import org.junit.Test
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Callable
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SpringModelReaderTest {

    private val settings = TranslationSettings()

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

        assertEquals(HTTPMethod.POST, endpointGroup.endpointByName(("foo")).method)
        assertEquals(HTTPMethod.GET, endpointGroup.endpointByName(("bar")).method)
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

        val group = readModel<ControllerWithVariables>().endpointGroups.single()

        assertEquals(ApiType.Class("Foo"), group.endpointByName("foo").responseBody)
    }

    @Test
    fun wrappedResultTypes() {

        class Foo

        @RestController
        class MyController {

            @Suppress("unused")
            @RequestMapping("/foo")
            fun foo(): ResponseEntity<Foo> = error("dummy")

            @Suppress("unused")
            @RequestMapping("/bar")
            fun bar(): HttpEntity<Foo> = error("dummy")

            @Suppress("unused")
            @RequestMapping("/bar")
            fun baz(): Callable<Foo> = error("dummy")
        }

        val group = readModel<MyController>().endpointGroups.single()

        assertEquals(ApiType.Class("Foo"), group.endpointByName("foo").responseBody)
        assertEquals(ApiType.Class("Foo"), group.endpointByName("bar").responseBody)
        assertEquals(ApiType.Class("Foo"), group.endpointByName("baz").responseBody)
    }


    private fun EndpointGroup.endpointByName(name: String): Endpoint =
        assertNotNull(endpoints.find { it.name == name })

    private inline fun <reified T : Any> readModel(): ApiDefinition {
        val model = JavaModel()
        model.loadClassesFromInheritanceTree<T>()
        model.loadClassesFromInheritanceTree<PostMapping>()
        model.loadClassesFromInheritanceTree<GetMapping>()
        return SpringModelReader.readApiDefinition(model, settings)
    }
}

