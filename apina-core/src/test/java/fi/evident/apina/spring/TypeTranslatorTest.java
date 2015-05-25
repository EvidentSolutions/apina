package fi.evident.apina.spring;

import org.junit.Test;

import static fi.evident.apina.spring.TypeTranslator.translateName;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TypeTranslatorTest {

    @Test
    public void translatingNamesUnqualified() {
        assertThat(translateName("Foo"), is("Foo"));
    }

    @Test
    public void translatingQualifiedNames() {
        assertThat(translateName("foo.bar.Baz"), is("Baz"));
    }

    @Test
    public void translatingInnerClassNames() {
        assertThat(translateName("foo.bar.Baz$Quux"), is("Quux"));
    }
}
