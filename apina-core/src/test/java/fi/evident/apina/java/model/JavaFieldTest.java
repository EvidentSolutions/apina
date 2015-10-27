package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaFieldTest {

    @Test
    public void modifiers() {
        assertThat(fieldWithModifiers(0).isStatic(), is(false));
        assertThat(fieldWithModifiers(0).isEnumConstant(), is(false));
        assertThat(fieldWithModifiers(Opcodes.ACC_STATIC).isStatic(), is(true));
        assertThat(fieldWithModifiers(Opcodes.ACC_ENUM).isEnumConstant(), is(true));
    }

    private static JavaField fieldWithModifiers(int modifiers) {
        return new JavaField("foo", JavaVisibility.PUBLIC, new JavaBasicType("java.lang.String"), modifiers);
    }
}
