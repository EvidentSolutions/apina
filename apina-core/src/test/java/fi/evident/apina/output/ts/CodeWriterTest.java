package fi.evident.apina.output.ts;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CodeWriterTest {

    private final CodeWriter writer = new CodeWriter();

    @Test
    public void indents() {
        writer.writeLine("foo ");
        writer.writeBlock(() -> {
            writer.writeLine("bar");
            writer.writeLine("baz");
        });
        writer.writeLine();

        assertThat(writer.getOutput(), is("foo \n{\n    bar\n    baz\n}\n"));
    }

    @Test
    public void stringValues() {
        writer.writeValue("foo 'bar' baz\nquux");

        assertThat(writer.getOutput(), is("'foo \\'bar\\' baz\\nquux'"));
    }

    @Test
    public void numberValues() {
        writer.writeValue(42);

        assertThat(writer.getOutput(), is("42"));
    }

    @Test
    public void booleanValues() {
        writer.writeValue(false);

        assertThat(writer.getOutput(), is("false"));
    }

    @Test
    public void mapValues() {
        Map<String,Integer> map = new LinkedHashMap<>();
        map.put("foo", 1);
        map.put("bar", 2);
        map.put("baz", 3);
        writer.writeValue(map);

        assertThat(writer.getOutput(), is("{\n    'foo': 1,\n    'bar': 2,\n    'baz': 3\n}"));
    }

    @Test
    public void emptyMapValues() {
        writer.writeValue(emptyMap());

        assertThat(writer.getOutput(), is("{}"));
    }

    @Test
    public void collectionValues() {
        writer.writeValue(asList(1, 2, 3));

        assertThat(writer.getOutput(), is("[1, 2, 3]"));
    }
}
