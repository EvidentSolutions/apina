package fi.evident.apina.spring;

import fi.evident.apina.model.URITemplate;
import org.junit.Test;

import static fi.evident.apina.spring.SpringUriTemplateParser.parseUriTemplate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SpringUriTemplateParserTest {

    @Test
    public void templateWithoutVariables() {
        assertThat(parseUriTemplate(""), is(new URITemplate("")));
        assertThat(parseUriTemplate("foo"), is(new URITemplate("foo")));
        assertThat(parseUriTemplate("/foo"), is(new URITemplate("/foo")));
    }

    @Test
    public void templateWithSimpleVariables() {
        assertThat(parseUriTemplate("foo/{bar}/{baz}"), is(new URITemplate("foo/{bar}/{baz}")));
    }

    @Test
    public void templateWithVariableConstraints() {
        assertThat(parseUriTemplate("foo/{bar:\\d+}/{baz:[ab]}"), is(new URITemplate("foo/{bar}/{baz}")));
    }
}
