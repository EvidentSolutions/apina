package fi.evident.apina.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TypeNameTest {

    @Test
    public void equality() {
        assertThat(new TypeName("foo.Bar"), is(new TypeName("foo.Bar")));
        assertThat(new TypeName("foo.Bar"), is(not(new TypeName("foo.Baz"))));
    }
}
