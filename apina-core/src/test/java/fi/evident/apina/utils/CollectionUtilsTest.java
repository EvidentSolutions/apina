package fi.evident.apina.utils;

import org.junit.Test;

import java.util.Optional;

import static fi.evident.apina.utils.CollectionUtils.concat;
import static fi.evident.apina.utils.CollectionUtils.optionalToStream;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CollectionUtilsTest {

    @Test
    public void optionalStream() {
        assertThat(optionalToStream(Optional.empty()).collect(toList()), is(emptyList()));
        assertThat(optionalToStream(Optional.of("foo")).collect(toList()), is(singletonList("foo")));
    }

    @Test
    public void concatenation() {
        assertThat(concat(emptyList(), emptySet()), is(emptyList()));
        assertThat(concat(asList("foo", "bar"), asList("baz", "quux", "xyzzy")),
                is(asList("foo", "bar", "baz", "quux", "xyzzy")));
    }
}
