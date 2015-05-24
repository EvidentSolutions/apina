package fi.evident.apina.model.type;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ApiBasicTypeTest {

    @Test
    public void equality() {
        assertThat(new ApiBasicType("foo.Bar"), is(new ApiBasicType("foo.Bar")));
        assertThat(new ApiBasicType("foo.Bar"), is(not(new ApiBasicType("foo.Baz"))));
    }
}
