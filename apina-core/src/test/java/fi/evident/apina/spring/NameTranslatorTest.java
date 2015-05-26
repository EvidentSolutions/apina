package fi.evident.apina.spring;

import org.junit.Test;

import static fi.evident.apina.spring.NameTranslator.translateClassName;
import static fi.evident.apina.spring.NameTranslator.translateEndpointGroupName;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NameTranslatorTest {

    @Test
    public void translatingNamesUnqualified() {
        assertThat(translateClassName("Foo"), is("Foo"));
    }

    @Test
    public void translatingQualifiedNames() {
        assertThat(translateClassName("foo.bar.Baz"), is("Baz"));
    }

    @Test
    public void translatingInnerClassNames() {
        assertThat(translateClassName("foo.bar.Baz$Quux"), is("Quux"));
    }

    @Test
    public void removeControllerSuffixFromEndpointNames() {
        assertThat(translateEndpointGroupName("foo.Bar"), is("Bar"));
        assertThat(translateEndpointGroupName("foo.BarController"), is("Bar"));
    }
}
