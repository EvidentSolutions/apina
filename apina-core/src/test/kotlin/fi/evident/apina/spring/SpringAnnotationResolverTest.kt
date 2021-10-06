package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.reader.TestClassMetadataLoader
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpringAnnotationResolverTest {

    private val loader = TestClassMetadataLoader()
    private val model = JavaModel(loader)
    private val resolver = SpringAnnotationResolver(model)

    @Test
    fun `implied annotations`() {
        loader.loadClassesFromInheritanceTree<MyClass>()

        val implied = resolver.findImpliedAnnotations(JavaAnnotation(JavaType.basic<MyAnnotation>()))
        assertTrue(implied.any { it.name == JavaType.basic<MyAnnotation>() })
        assertTrue(implied.any { it.name == JavaType.basic<MyMetaAnnotation>() })
        assertTrue(implied.any { it.name == JavaType.basic<MyMetaMetaAnnotation>() })
    }

    @Test
    fun `find annotation`() {
        loader.loadClassesFromInheritanceTree<MyClass>()

        val clazz = model.findClass(JavaType.basic<MyClass>())!!

        assertNotNull(resolver.findAnnotation(clazz, JavaType.basic<MyAnnotation>()))
        assertNotNull(resolver.findAnnotation(clazz, JavaType.basic<MyMetaAnnotation>()))
        assertNotNull(resolver.findAnnotation(clazz, JavaType.basic<MyMetaMetaAnnotation>()))
    }

    @Test
    fun `attributes without aliases`() {
        loader.loadClassesFromInheritanceTree<MyController>()

        val requestMapping = getMethodAnnotation<MyController>("postMethod", JavaType.basic<RequestMapping>())
        assertEquals("POST", requestMapping.getAttribute<EnumValue>("method")?.constant)
    }

    @Test
    fun `meta annotations`() {
        loader.loadClassesFromInheritanceTree<MyController>()
        val controller = requireNotNull(model.findClass(JavaType.basic<MyController>()))

        assertTrue(resolver.hasAnnotation(controller, JavaType.basic<RestController>()))
        assertNotNull(resolver.findAnnotation(controller, JavaType.basic<RestController>()))

        val requestMapping = resolver.getAnnotation<RequestMapping>(controller)
        assertEquals("/base-path", requestMapping.getAttribute("path"))
        assertEquals("/base-path", requestMapping.getAttribute("value"))
    }

    @Test
    fun `resolve implicit aliases`() {
        loader.loadClassesFromInheritanceTree<MyController>()
        loader.loadClassesFromInheritanceTree<GetMapping>()

        val fooAnnotation = getMethodAnnotation<MyController>("foo", JavaType.basic<RequestMapping>())
        val barAnnotation = getMethodAnnotation<MyController>("bar", JavaType.basic<RequestMapping>())

        assertEquals("/foo", fooAnnotation.getAttribute("path"))
        assertEquals("/foo", fooAnnotation.getAttribute("value"))

        assertEquals("/bar", barAnnotation.getAttribute("path"))
        assertEquals("/bar", barAnnotation.getAttribute("value"))
    }

    private inline fun <reified T : Any> getMethodAnnotation(method: String, annotationType: JavaType.Basic) =
        requireNotNull(resolver.findAnnotation(model.getMethod<T>(method), annotationType))

    private inline fun <reified T : Any> JavaModel.getMethod(method: String): JavaMethod =
        requireNotNull(findClass(JavaType.basic<T>())?.methods?.find { it.name == method })

    private inline fun <reified T : Annotation> SpringAnnotationResolver.getAnnotation(element: JavaAnnotatedElement): SpringAnnotation =
        requireNotNull(findAnnotation(element, JavaType.basic<T>()))

    @Retention(AnnotationRetention.RUNTIME)
    private annotation class MyMetaMetaAnnotation

    @Retention(AnnotationRetention.RUNTIME)
    @MyMetaMetaAnnotation
    private annotation class MyMetaAnnotation

    @Retention(AnnotationRetention.RUNTIME)
    @MyMetaAnnotation
    private annotation class MyAnnotation

    @MyAnnotation
    private interface InterfaceWithAnnotation

    @MyAnnotation
    private class MyClass : InterfaceWithAnnotation

    @Retention(AnnotationRetention.RUNTIME)
    @RestController
    private annotation class MyRestController

    @Retention(AnnotationRetention.RUNTIME)
    @RequestMapping("/base-path")
    private annotation class MyRequestMapping

    @MyRestController
    @MyRequestMapping
    @SuppressWarnings("unused")
    class MyController {

        @GetMapping("/foo")
        fun foo() {
        }

        @GetMapping(path = ["/bar"])
        fun bar() {
        }

        @RequestMapping(method = [RequestMethod.POST])
        fun postMethod() {
        }
    }
}
