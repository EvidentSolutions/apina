package fi.evident.apina.spring.java.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AnnotationMetadataTest {

    @Test
    public void toStringWithoutParameters() {
        AnnotationMetadata annotation = new AnnotationMetadata(new QualifiedName("foo.bar.Baz"));
        assertThat(annotation.toString(), is("@foo.bar.Baz"));
    }

    @Test
    public void toStringWithJustValue() {
        AnnotationMetadata annotation = new AnnotationMetadata(new QualifiedName("foo.bar.Baz"));
        annotation.setAttribute("value", 42);

        assertThat(annotation.toString(), is("@foo.bar.Baz(42)"));
    }

    @Test
    public void toStringWithNonValueAttribute() {
        AnnotationMetadata annotation = new AnnotationMetadata(new QualifiedName("foo.bar.Baz"));
        annotation.setAttribute("foo", 42);

        assertThat(annotation.toString(), is("@foo.bar.Baz(foo=42)"));
    }

    @Test
    public void toStringWithMultipleAttributes() {
        AnnotationMetadata annotation = new AnnotationMetadata(new QualifiedName("foo.bar.Baz"));
        annotation.setAttribute("value", 1);
        annotation.setAttribute("foo", 2);
        annotation.setAttribute("bar", 3);

        assertThat(annotation.toString(), is("@foo.bar.Baz(value=1, foo=2, bar=3)"));
    }
}
