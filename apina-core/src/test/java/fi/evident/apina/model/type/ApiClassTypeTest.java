package fi.evident.apina.model.type;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ApiClassTypeTest {

    @Test
    public void equality() {
        assertThat(new ApiClassType(new ApiTypeName("foo.Bar")), is(new ApiClassType(new ApiTypeName("foo.Bar"))));
        assertThat(new ApiClassType(new ApiTypeName("foo.Bar")), is(not(new ApiClassType(new ApiTypeName("foo.Baz")))));
    }
}
