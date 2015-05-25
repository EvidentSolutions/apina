package fi.evident.apina.model.type;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ApiClassTypeTest {

    @Test
    public void equality() {
        assertThat(new ApiClassType("foo.Bar"), is(new ApiClassType("foo.Bar")));
        assertThat(new ApiClassType("foo.Bar"), is(not(new ApiClassType("foo.Baz"))));
    }
}
