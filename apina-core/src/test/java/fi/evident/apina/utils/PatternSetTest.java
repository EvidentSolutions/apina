package fi.evident.apina.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PatternSetTest {

    private final PatternSet set = new PatternSet();

    @Test
    public void emptySetMatchesNothing() {
        assertThat(set.test(""), is(false));
        assertThat(set.test("foo"), is(false));
    }

    @Test
    public void setWithSinglePatternMatchesIfPatternMatches() {
        set.addPattern("foo.*");

        assertThat(set.test(""), is(false));
        assertThat(set.test("bar"), is(false));
        assertThat(set.test("foo"), is(true));
        assertThat(set.test("foobar"), is(true));
    }

    @Test
    public void setWithMultiplePatternsMatchesIfAnyPatternMatches() {
        set.addPattern("foo.*");
        set.addPattern("bar.*");
        set.addPattern("baz.*");

        assertThat(set.test(""), is(false));

        assertThat(set.test("foo"), is(true));
        assertThat(set.test("bar"), is(true));
        assertThat(set.test("baz"), is(true));
        assertThat(set.test("baz-quux"), is(true));

        assertThat(set.test("quux"), is(false));
    }
}
