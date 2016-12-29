package fi.evident.apina.spring

import fi.evident.apina.model.URITemplate
import org.junit.Assert.assertEquals
import org.junit.Test

class SpringUriTemplateParserTest {

    @Test
    fun templateWithoutVariables() {
        assertEquals(URITemplate(""), parseUriTemplate(""))
        assertEquals(URITemplate("foo"), parseUriTemplate("foo"))
        assertEquals(URITemplate("/foo"), parseUriTemplate("/foo"))
    }

    @Test
    fun templateWithSimpleVariables() {
        assertEquals(URITemplate("foo/{bar}/{baz}"), parseUriTemplate("foo/{bar}/{baz}"))
    }

    @Test
    fun templateWithVariableConstraints() {
        assertEquals(URITemplate("foo/{bar}/{baz}"), parseUriTemplate("foo/{bar:\\d+}/{baz:[ab]}"))
    }

    @Test
    fun templatesWithClosingBracesInsideConstraints() {
        assertEquals(URITemplate("/{id}/foo"), parseUriTemplate("/{id:[a-z]{16}[0-9]{4}}/foo"))
    }

    @Test
    fun templatesWithEscapedBraces() {
        assertEquals(URITemplate("/{id}/foo"), parseUriTemplate("/{id:\\}}/foo"))
    }
}
