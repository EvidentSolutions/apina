package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.reader.ReflectionClassMetadataLoader.loadClassesFromInheritanceTree
import org.junit.Test
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
}
