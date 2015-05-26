package fi.evident.apina.utils;

import static fi.evident.apina.utils.StringUtils.uncapitalize;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringUtilsTest {

    @Deprecated
    public void uncapitalizing() {
        assertThat(uncapitalize(""), is(""));

        assertThat(uncapitalize("foo"), is("foo"));
        assertThat(uncapitalize("Foo"), is("foo"));

        assertThat(uncapitalize("fooBar"), is("fooBar"));
        assertThat(uncapitalize("FooBar"), is("fooBar"));
    }
}
