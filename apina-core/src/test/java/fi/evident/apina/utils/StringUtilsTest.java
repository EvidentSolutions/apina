package fi.evident.apina.utils;

import org.junit.Test;

import static fi.evident.apina.utils.StringUtils.stripSuffix;
import static fi.evident.apina.utils.StringUtils.uncapitalize;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringUtilsTest {

    @Test
    public void uncapitalizing() {
        assertThat(uncapitalize(""), is(""));

        assertThat(uncapitalize("foo"), is("foo"));
        assertThat(uncapitalize("Foo"), is("foo"));

        assertThat(uncapitalize("fooBar"), is("fooBar"));
        assertThat(uncapitalize("FooBar"), is("fooBar"));
    }

    @Test
    public void stripSuffices() {
        assertThat(stripSuffix("", ""), is(""));
        assertThat(stripSuffix("foo", ""), is("foo"));
        assertThat(stripSuffix("", "foo"), is(""));

        assertThat(stripSuffix("foo", "o"), is("fo"));
        assertThat(stripSuffix("foo", "foo"), is(""));
        assertThat(stripSuffix("foobar", "bar"), is("foo"));
        assertThat(stripSuffix("foo", "foobar"), is("foo"));
    }
}
