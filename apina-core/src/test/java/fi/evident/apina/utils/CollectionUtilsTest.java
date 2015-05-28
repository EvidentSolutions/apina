package fi.evident.apina.utils;

import org.junit.Test;

import java.util.Optional;

import static fi.evident.apina.utils.CollectionUtils.optionalToStream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CollectionUtilsTest {

    @Test
    public void optionalStream() {
        assertThat(optionalToStream(Optional.empty()).collect(toList()), is(emptyList()));
        assertThat(optionalToStream(Optional.of("foo")).collect(toList()), is(singletonList("foo")));
    }
}
