package fi.evident.apina.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropertyUtilsTest {

    @Test
    fun `property names for value getters`() {
        assertEquals("foo", propertyNameForGetter("getFoo-11MJ8YI"))
        assertEquals("fooBar", propertyNameForGetter("getFooBar-11MJ8YI"))
        assertEquals("foo", propertyNameForGetter("isFoo-11MJ8YI"))
        assertEquals("fooBar", propertyNameForGetter("isFooBar-11MJ8YI"))
    }

    @Test
    fun `property names for getters`() {
        assertEquals("foo", propertyNameForGetter("getFoo"))
        assertEquals("fooBar", propertyNameForGetter("getFooBar"))
        assertEquals("foo", propertyNameForGetter("isFoo"))
        assertEquals("fooBar", propertyNameForGetter("isFooBar"))
    }
}
