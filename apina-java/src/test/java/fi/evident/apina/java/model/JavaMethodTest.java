package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaMethodTest {

    @Test
    public void modifiers() {
        assertThat(methodWithModifiers(0).isStatic(), is(false));
        assertThat(methodWithModifiers(Opcodes.ACC_STATIC).isStatic(), is(true));
    }

    private static JavaMethod methodWithModifiers(int modifiers) {
        return new JavaMethod("foo", JavaVisibility.PUBLIC, new JavaBasicType("java.lang.String"), emptyList(), modifiers);
    }
}
