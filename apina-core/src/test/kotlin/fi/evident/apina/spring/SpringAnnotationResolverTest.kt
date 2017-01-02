package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotatedElement
import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.JavaMethod
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.reader.ReflectionClassMetadataLoader.loadClassesFromInheritanceTree
import org.junit.Ignore
import org.junit.Test
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpringAnnotationResolverTest {

    @Test
    fun impliedAnnotations() {
        val model = loadClassesFromInheritanceTree(MyClass::class.java)
        val resolver = SpringAnnotationResolver(model)

        val implied = resolver.findImpliedAnnotations(JavaAnnotation(JavaType.basic<MyAnnotation>()))
        assertTrue(implied.any { it.name == JavaType.basic<MyAnnotation>() })
        assertTrue(implied.any { it.name == JavaType.basic<MyMetaAnnotation>() })
        assertTrue(implied.any { it.name == JavaType.basic<MyMetaMetaAnnotation>() })
    }

    @Test
    fun findAnnotation() {
        val model = loadClassesFromInheritanceTree(MyClass::class.java)
        val resolver = SpringAnnotationResolver(model)

        val clazz = model.findClass(JavaType.basic<MyClass>())!!

        assertNotNull(resolver.findAnnotation(clazz, JavaType.basic<MyAnnotation>()))
        assertNotNull(resolver.findAnnotation(clazz, JavaType.basic<MyMetaAnnotation>()))
        assertNotNull(resolver.findAnnotation(clazz, JavaType.basic<MyMetaMetaAnnotation>()))
    }

    @Test
    fun metaAnnotations() {
        val model = loadClassesFromInheritanceTree(MyController::class.java)
        val resolver = SpringAnnotationResolver(model)
        val controller = model.findClass(JavaType.basic<MyController>())!!

        assertTrue(resolver.hasAnnotation(controller, JavaType.basic<RestController>()))
        assertNotNull(resolver.findAnnotation(controller, JavaType.basic<RestController>()))

        val requestMapping = resolver.getAnnotation<RequestMapping>(controller)
        assertEquals("/base-path", requestMapping.getAttribute<String>("path"))
        assertEquals("/base-path", requestMapping.getAttribute<String>("value"))
    }

    @Test
    @Ignore("Implicit aliases are not supported yet")
    fun resolveImplicitAliases() {
        val model = loadClassesFromInheritanceTree(MyController::class.java)
        val resolver = SpringAnnotationResolver(model)

        val fooAnnotation = resolver.getAnnotation<GetMapping>(model.getMethod<MyController>("foo"))
        val barAnnotation = resolver.getAnnotation<GetMapping>(model.getMethod<MyController>("bar"))

        assertEquals("/foo", fooAnnotation.getAttribute<String>("value"))
        assertEquals("/foo", fooAnnotation.getAttribute<String>("path"))

        assertEquals("/bar", barAnnotation.getAttribute<String>("value"))
        assertEquals("/bar", barAnnotation.getAttribute<String>("path"))
    }

    private inline fun <reified T : Any> JavaModel.getMethod(method: String): JavaMethod =
        findClass(JavaType.basic<T>())?.methods?.find { it.name == method }!!

    private inline fun <reified T : Annotation> SpringAnnotationResolver.getAnnotation(element: JavaAnnotatedElement): SpringAnnotation =
        findAnnotation(element, JavaType.basic<T>())!!

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
    class MyController {

        @GetMapping("/foo")
        fun foo() { }

        @GetMapping(path = arrayOf("/bar"))
        fun bar() { }
    }
}
