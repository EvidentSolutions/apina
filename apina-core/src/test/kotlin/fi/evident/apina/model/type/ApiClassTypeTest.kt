package fi.evident.apina.model.type

import fi.evident.apina.model.type.ApiType.Class
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ApiClassTypeTest {

    @Test
    fun equality() {
        assertEquals(Class(ApiTypeName("foo.Bar")), Class(ApiTypeName("foo.Bar")))
        assertNotEquals(Class(ApiTypeName("foo.Bar")), Class(ApiTypeName("foo.Baz")))
    }
}
