package fi.evident.apina.java.model;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaAnnotationTest {

    @Test
    public void toStringWithoutParameters() {
        JavaAnnotation annotation = newAnnotation("foo.bar.Baz");
        assertThat(annotation.toString(), is("@foo.bar.Baz"));
    }

    @Test
    public void toStringWithJustValue() {
        JavaAnnotation annotation = newAnnotation("foo.bar.Baz");
        annotation.setAttribute("value", 42);

        assertThat(annotation.toString(), is("@foo.bar.Baz(42)"));
    }

    @Test
    public void toStringWithNonValueAttribute() {
        JavaAnnotation annotation = newAnnotation("foo.bar.Baz");
        annotation.setAttribute("foo", 42);

        assertThat(annotation.toString(), is("@foo.bar.Baz(foo=42)"));
    }

    @Test
    public void toStringWithMultipleAttributes() {
        JavaAnnotation annotation = newAnnotation("foo.bar.Baz");
        annotation.setAttribute("value", 1);
        annotation.setAttribute("foo", 2);
        annotation.setAttribute("bar", 3);

        assertThat(annotation.toString(), is("@foo.bar.Baz(value=1, foo=2, bar=3)"));
    }

    @Test
    public void arrayValues() {
        JavaAnnotation annotation = newAnnotation("foo.bar.Baz");
        annotation.setAttribute("array", new Object[] { 1, 2, 3 });
        annotation.setAttribute("single", 1);

        assertThat(annotation.getAttributeValues("array"), is(asList(1, 2, 3)));
        assertThat(annotation.getAttributeValues("single"), is(singletonList(1)));
        assertThat(annotation.getAttributeValues("nonexistent"), is(emptyList()));
    }

    private static JavaAnnotation newAnnotation(String name) {
        return new JavaAnnotation(new QualifiedName(name));
    }
}
