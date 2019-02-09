package fi.evident.apina.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class URITemplateTest {

    @Test
    fun toStringRepresentation() {
        assertEquals("/foo", URITemplate("/foo").toString())
    }
}
