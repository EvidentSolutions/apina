package fi.evident.apina.java.model.type;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class JavaBasicTypeTest {

    @Test
    public void primitiveNumbers() {
        assertThat(new JavaBasicType(int.class).isPrimitiveNumber(), is(true));
        assertThat(new JavaBasicType(long.class).isPrimitiveNumber(), is(true));
        assertThat(new JavaBasicType(short.class).isPrimitiveNumber(), is(true));
        assertThat(new JavaBasicType(float.class).isPrimitiveNumber(), is(true));
        assertThat(new JavaBasicType(double.class).isPrimitiveNumber(), is(true));

        assertThat(new JavaBasicType(boolean.class).isPrimitiveNumber(), is(false));
        assertThat(new JavaBasicType(char.class).isPrimitiveNumber(), is(false));
        assertThat(new JavaBasicType(Integer.class).isPrimitiveNumber(), is(false));
        assertThat(new JavaBasicType(String.class).isPrimitiveNumber(), is(false));
    }

    @Test
    public void wideTypes() {
        assertEquals(false, new JavaBasicType(int.class).isWide());
        assertEquals(false, new JavaBasicType(float.class).isWide());
        assertEquals(true, new JavaBasicType(long.class).isWide());
        assertEquals(true, new JavaBasicType(double.class).isWide());
    }
}
