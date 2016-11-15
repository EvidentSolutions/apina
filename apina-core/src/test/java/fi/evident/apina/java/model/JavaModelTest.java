package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.reader.ReflectionClassMetadataLoader;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.util.Set;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaModelTest {

    @Test
    public void impliedAnnotations() {
        JavaModel model = ReflectionClassMetadataLoader.loadClassesFromInheritanceTree(MyClass.class);

        JavaBasicType metaMeta = new JavaBasicType(MyMetaMetaAnnotation.class);

        Set<JavaBasicType> annotations = model.findAnnotationsImpliedBy(metaMeta);
        assertThat(annotations.size(), is(3));
        assertThat(annotations, hasItem(new JavaBasicType(MyMetaMetaAnnotation.class)));
        assertThat(annotations, hasItem(new JavaBasicType(MyMetaAnnotation.class)));
        assertThat(annotations, hasItem(new JavaBasicType(MyAnnotation.class)));
    }

    @Retention(RUNTIME)
    private @interface MyMetaMetaAnnotation { }

    @Retention(RUNTIME)
    @MyMetaMetaAnnotation
    private @interface MyMetaAnnotation { }

    @Retention(RUNTIME)
    @MyMetaAnnotation
    private @interface MyAnnotation { }

    @MyAnnotation
    private interface InterfaceWithAnnotation { }

    @MyAnnotation
    private static class MyClass implements InterfaceWithAnnotation { }
}
