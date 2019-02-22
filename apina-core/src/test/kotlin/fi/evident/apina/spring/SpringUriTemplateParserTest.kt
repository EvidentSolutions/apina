package fi.evident.apina.spring

import fi.evident.apina.model.URITemplate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SpringUriTemplateParserTest {

    @Test
    fun `template without variables`() {
        assertEquals(URITemplate(""), parseSpringUriTemplate(""))
        assertEquals(URITemplate("foo"), parseSpringUriTemplate("foo"))
        assertEquals(URITemplate("/foo"), parseSpringUriTemplate("/foo"))
    }

    @Test
    fun `template with simple variables`() {
        assertEquals(URITemplate("foo/{bar}/{baz}"), parseSpringUriTemplate("foo/{bar}/{baz}"))
    }

    @Test
    fun `template with variable constraints`() {
        assertEquals(URITemplate("foo/{bar}/{baz}"), parseSpringUriTemplate("foo/{bar:\\d+}/{baz:[ab]}"))
    }

    @Test
    fun `templates with closing braces inside constraints`() {
        assertEquals(URITemplate("/{id}/foo"), parseSpringUriTemplate("/{id:[a-z]{16}[0-9]{4}}/foo"))
    }

    @Test
    fun `templates with escaped braces`() {
        assertEquals(URITemplate("/{id}/foo"), parseSpringUriTemplate("/{id:\\}}/foo"))
    }
}
