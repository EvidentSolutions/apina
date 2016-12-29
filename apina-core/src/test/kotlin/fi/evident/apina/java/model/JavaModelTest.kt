package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.reader.ReflectionClassMetadataLoader.loadClassesFromInheritanceTree
import org.hamcrest.CoreMatchers.hasItem
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.test.assertEquals

class JavaModelTest {

    @Test
    fun impliedAnnotations() {
        val model = loadClassesFromInheritanceTree(MyClass::class.java)

        val metaMeta = JavaType.Basic(MyMetaMetaAnnotation::class.java)

        val annotations = model.findAnnotationsImpliedBy(metaMeta)
        assertEquals(3, annotations.size)
        assertThat(annotations, hasItem(JavaType.Basic(MyMetaMetaAnnotation::class.java)))
        assertThat(annotations, hasItem(JavaType.Basic(MyMetaAnnotation::class.java)))
        assertThat(annotations, hasItem(JavaType.Basic(MyAnnotation::class.java)))
    }

    @Retention(RUNTIME)
    private annotation class MyMetaMetaAnnotation

    @Retention(RUNTIME)
    @MyMetaMetaAnnotation
    private annotation class MyMetaAnnotation

    @Retention(RUNTIME)
    @MyMetaAnnotation
    private annotation class MyAnnotation

    @MyAnnotation
    private interface InterfaceWithAnnotation

    @MyAnnotation
    private class MyClass : InterfaceWithAnnotation
}
