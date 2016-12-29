package fi.evident.apina.utils

import org.junit.Test
import kotlin.test.assertEquals

class PropertyUtilsTest {

    @Test
    fun propertyNamesForGetters() {
        assertEquals("foo", propertyNameForGetter("getFoo"))
        assertEquals("fooBar", propertyNameForGetter("getFooBar"))
        assertEquals("foo", propertyNameForGetter("isFoo"))
        assertEquals("fooBar", propertyNameForGetter("isFooBar"))
    }
}
