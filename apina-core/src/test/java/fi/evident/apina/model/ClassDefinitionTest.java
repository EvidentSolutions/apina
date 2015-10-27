package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiPrimitiveType;
import fi.evident.apina.model.type.ApiTypeName;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClassDefinitionTest {

    private final ClassDefinition classDefinition = new ClassDefinition(new ApiTypeName("foo.Bar"));

    @Test(expected = IllegalArgumentException.class)
    public void duplicatePropertiesAreDisallowed() {
        classDefinition.addProperty(arbitraryProperty("foo"));
        classDefinition.addProperty(arbitraryProperty("foo"));
    }

    @Test
    public void hasProperty() {
        assertThat(classDefinition.hasProperty("foo"), is(false));
        assertThat(classDefinition.hasProperty("bar"), is(false));

        classDefinition.addProperty(arbitraryProperty("foo"));

        assertThat(classDefinition.hasProperty("foo"), is(true));
        assertThat(classDefinition.hasProperty("bar"), is(false));
    }

    private static PropertyDefinition arbitraryProperty(String name) {
        return new PropertyDefinition(name, ApiPrimitiveType.STRING);
    }
}
