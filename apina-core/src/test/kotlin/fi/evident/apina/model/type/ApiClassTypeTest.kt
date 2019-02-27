package fi.evident.apina.model.type

import fi.evident.apina.model.type.ApiType.Class
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ApiClassTypeTest {

    @Test
    fun equality() {
        assertEquals(Class(ApiTypeName("foo.Bar"), emptyList()), Class(ApiTypeName("foo.Bar"), emptyList()))
        assertNotEquals(Class(ApiTypeName("foo.Bar"), emptyList()), Class(ApiTypeName("foo.Baz"), emptyList()))
    }
}
