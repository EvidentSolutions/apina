package fi.evident.apina.spring.java.model;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaFieldTest {

    @Test
    public void modifiers() {
        assertThat(fieldWithModifiers(0).isStatic(), is(false));
        assertThat(fieldWithModifiers(Opcodes.ACC_STATIC).isStatic(), is(true));
    }

    private static JavaField fieldWithModifiers(int modifiers) {
        return new JavaField("foo", JavaVisibility.PUBLIC, new JavaType(new QualifiedName("java.lang.String")), modifiers);
    }
}
