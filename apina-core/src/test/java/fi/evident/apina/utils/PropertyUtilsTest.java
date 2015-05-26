package fi.evident.apina.utils;

import org.junit.Test;

import static fi.evident.apina.utils.PropertyUtils.propertyNameForGetter;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PropertyUtilsTest {

    @Test
    public void propertyNamesForGetters() {
        assertThat(propertyNameForGetter("getFoo"), is("foo"));
        assertThat(propertyNameForGetter("getFooBar"), is("fooBar"));
        assertThat(propertyNameForGetter("isFoo"), is("foo"));
        assertThat(propertyNameForGetter("isFooBar"), is("fooBar"));
    }
}
