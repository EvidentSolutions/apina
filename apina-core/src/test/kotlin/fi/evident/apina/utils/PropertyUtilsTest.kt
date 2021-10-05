package fi.evident.apina.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropertyUtilsTest {

    @Test
    fun propertyNamesForValueGetters() {
        assertEquals("foo", propertyNameForGetter("getFoo-11MJ8YI"))
        assertEquals("fooBar", propertyNameForGetter("getFooBar-11MJ8YI"))
        assertEquals("foo", propertyNameForGetter("isFoo-11MJ8YI"))
        assertEquals("fooBar", propertyNameForGetter("isFooBar-11MJ8YI"))
    }

    @Test
    fun propertyNamesForGetters() {
        assertEquals("foo", propertyNameForGetter("getFoo"))
        assertEquals("fooBar", propertyNameForGetter("getFooBar"))
        assertEquals("foo", propertyNameForGetter("isFoo"))
        assertEquals("fooBar", propertyNameForGetter("isFooBar"))
    }
}
