package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.reader.TestClassMetadataLoader
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.Endpoint
import fi.evident.apina.model.EndpointGroup
import fi.evident.apina.model.HTTPMethod
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.Callable
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SpringModelReaderTest {

    private val settings = TranslationSettings()

    @Test
    fun `parse simple controller`() {
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
    fun `reference to generic class`() {
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
    @Suppress("unused")
    fun `scan methods in superclasses`() {

        abstract class SuperController {
            @GetMapping("/foo")
            fun foo() = ""
        }

        @RestController
        @RequestMapping("/prefix")
        class SubController : SuperController() {

            @GetMapping("/bar")
            fun bar() = ""
        }

        val group = readModel<SubController>().endpointGroups.single()

        assertEquals(2, group.endpoints.size)

        assertEquals("/prefix/foo", group.endpointByName("foo").uriTemplate.toString())
        assertEquals("/prefix/bar", group.endpointByName("bar").uriTemplate.toString())
    }

    @Test
    @Suppress("unused")
    fun `only most specific override is used`() {
        abstract class SuperController {
            @GetMapping("/foo")
            open fun foo() = ""
        }

        @RestController
        class SubController : SuperController() {

            @GetMapping("/bar")
            override fun foo() = ""
        }

        val group = readModel<SubController>().endpointGroups.single()

        assertEquals(1, group.endpoints.size)
        assertEquals("/bar", group.endpointByName("foo").uriTemplate.toString())
    }

    @Test
    @Suppress("unused")
    fun `resolve types from type-parameters`() {

        abstract class SuperController<T> {
            @GetMapping("/foo")
            open fun foo(@RequestParam s: T): T = error("")
        }

        @RestController
        class SubController : SuperController<String>()

        val group = readModel<SubController>().endpointGroups.single()

        val endpoint = group.endpointByName("foo")
        assertEquals(ApiType.Primitive.STRING, endpoint.responseBody)
        assertEquals(ApiType.Primitive.STRING, endpoint.parameters[0].type)
    }

    @Test
    @Suppress("unused")
    fun `resolve types from list typed parameters`() {

        @RestController
        @Suppress("unused")
        abstract class MyController {
            @GetMapping("/foo")
            fun foo(@RequestParam s: List<MyEnum>): String = error("")
        }

        val group = readModel<MyController>().endpointGroups.single()
        val endpoint = group.endpointByName("foo")

        assertEquals(ApiType.Array(ApiType.Class("MyEnum")), endpoint.parameters[0].type)
    }

    @Test
    fun `wrapped result types`() {

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

    @Test
    fun `extra arguments are not added to model`() {

        class Foo
        class Bar
        class Baz

        @RestController
        class MyController {

            @Suppress("unused")
            @RequestMapping("/foo")
            fun foo(@RequestBody foo: Foo, @RequestParam bar: Bar, baz: Baz) {}
        }

        val model = readModel(MyController::class, Foo::class, Bar::class, Baz::class)
        assertEquals(setOf("Foo", "Bar"), model.classDefinitions.map { it.type.name }.toSet())
    }

    private fun EndpointGroup.endpointByName(name: String): Endpoint =
        assertNotNull(endpoints.find { it.name == name })

    private inline fun <reified T : Any> readModel(): ApiDefinition =
        readModel(T::class)

    private fun readModel(vararg classes: KClass<*>): ApiDefinition {
        val model = JavaModel(TestClassMetadataLoader().apply {
            for (cl in classes)
                loadClassesFromInheritanceTree(cl.java)
            loadClassesFromInheritanceTree<PostMapping>()
            loadClassesFromInheritanceTree<GetMapping>()
        })

        return SpringModelReader.readApiDefinition(model, settings)
    }

    @Suppress("unused")
    private enum class MyEnum { C1, C2 }
}

