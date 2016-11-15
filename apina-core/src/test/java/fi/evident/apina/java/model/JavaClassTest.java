package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaClassTest {

    @Test
    public void interfaces() {
        assertThat(classWithModifiers(Modifier.PUBLIC).isInterface(), is(false));
        assertThat(classWithModifiers(Modifier.INTERFACE).isInterface(), is(true));
        assertThat(classWithModifiers(Modifier.INTERFACE | Modifier.PUBLIC).isInterface(), is(true));
    }

    @Test
    public void annotations() {
        assertThat(classWithModifiers(Modifier.INTERFACE).isAnnotation(), is(false));
        assertThat(classWithModifiersAndInterfaces(Modifier.INTERFACE, singletonList(new JavaBasicType(Annotation.class))).isAnnotation(), is(true));
    }

    private static JavaClass classWithModifiers(int modifiers) {
        return classWithModifiersAndInterfaces(modifiers, emptyList());
    }

    private static JavaClass classWithModifiersAndInterfaces(int modifiers, List<JavaType> interfaces) {
        return new JavaClass(new JavaBasicType("test.Foo"), new JavaBasicType("test.Bar"), interfaces, modifiers, new TypeSchema());
    }
}
