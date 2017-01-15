package fi.evident.apina.model

import org.junit.Test
import kotlin.test.assertEquals

class URITemplateTest {

    @Test
    fun toStringRepresentation() {
        assertEquals("/foo", URITemplate("/foo").toString())
    }
}
