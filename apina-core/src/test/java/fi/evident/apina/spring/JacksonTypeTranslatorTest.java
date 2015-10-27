package fi.evident.apina.spring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import fi.evident.apina.java.model.ClassMetadataCollection;
import fi.evident.apina.java.model.JavaClass;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.ClassDefinition;
import fi.evident.apina.model.EnumDefinition;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.*;
import org.junit.Test;

import java.util.*;

import static fi.evident.apina.model.ModelMatchers.hasProperties;
import static fi.evident.apina.model.ModelMatchers.property;
import static fi.evident.apina.spring.ReflectionClassMetadataLoader.loadClassesFromInheritanceTree;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unused")
public class JacksonTypeTranslatorTest {

    private final TranslationSettings settings = new TranslationSettings();

    @Test
    public void translatingClassWithFieldProperties() {
        ClassDefinition classDefinition = translateClass(ClassWithFieldProperties.class);

        assertThat(classDefinition.getType(), is(new ApiTypeName(ClassWithFieldProperties.class.getSimpleName())));
        assertThat(classDefinition.getProperties(), hasProperties(
                property("intField", ApiPrimitiveType.NUMBER),
                property("integerField", ApiPrimitiveType.NUMBER),
                property("stringField", ApiPrimitiveType.STRING),
                property("booleanField", ApiPrimitiveType.BOOLEAN),
                property("booleanNonPrimitiveField", ApiPrimitiveType.BOOLEAN),
                property("intArrayField", new ApiArrayType(ApiPrimitiveType.NUMBER)),
                property("rawCollectionField", new ApiArrayType(ApiPrimitiveType.ANY)),
                property("rawMapField", new ApiDictionaryType(ApiPrimitiveType.ANY)),
                property("stringIntegerMapField", new ApiDictionaryType(ApiPrimitiveType.NUMBER)),
                property("objectField", ApiPrimitiveType.ANY),
                property("stringCollectionField", new ApiArrayType(ApiPrimitiveType.STRING))));
    }

    @Test
    public void translatingClassWithGetterProperties() {
        ClassDefinition classDefinition = translateClass(ClassWithGetters.class);

        assertThat(classDefinition.getType(), is(new ApiTypeName(ClassWithGetters.class.getSimpleName())));
        assertThat(classDefinition.getProperties(), hasProperties(
                property("int", ApiPrimitiveType.NUMBER),
                property("integer", ApiPrimitiveType.NUMBER),
                property("string", ApiPrimitiveType.STRING),
                property("boolean", ApiPrimitiveType.BOOLEAN),
                property("booleanNonPrimitive", ApiPrimitiveType.BOOLEAN)));
    }

    @Test
    public void translatingClassWithOverlappingFieldAndGetter() {
        ClassDefinition classDefinition = translateClass(TypeWithOverlappingFieldAndGetter.class);

        assertThat(classDefinition.getProperties(), hasProperties(
                property("foo", ApiPrimitiveType.STRING)));
    }

    @Test
    public void translateVoidType() {
        assertThat(translateType(JavaBasicType.VOID), is(ApiPrimitiveType.VOID));
    }

    @Test
    public void translateBlackBoxType() {
        settings.blackBoxClasses.addPattern("foo\\..+");

        assertThat(translateType(new JavaBasicType("foo.bar.Baz")), is(new ApiBlackBoxType(new ApiTypeName("Baz"))));
    }

    @Test
    public void translatingOptionalTypes() {
        ClassDefinition classDefinition = translateClass(ClassWithOptionalTypes.class);

        assertThat(classDefinition.getType(), is(new ApiTypeName(ClassWithOptionalTypes.class.getSimpleName())));
        assertThat(classDefinition.getProperties(), hasProperties(
                property("optionalString", ApiPrimitiveType.STRING),
                property("optionalInt", ApiPrimitiveType.NUMBER),
                property("optionalLong", ApiPrimitiveType.NUMBER),
                property("optionalDouble", ApiPrimitiveType.NUMBER)));
    }

    @Test
    public void typesWithJsonValueShouldBeBlackBoxes() {
        ApiType apiType = translateClass(ClassWithJsonValue.class, new ApiDefinition());

        assertThat(apiType, is(new ApiBlackBoxType(new ApiTypeName("ClassWithJsonValue"))));
    }

    @Test(expected = DuplicateClassNameException.class)
    public void duplicateClassNames() {
        JavaClass class1 = new JavaClass(new JavaBasicType("foo.MyClass"), new JavaBasicType("java.lang.Object"), emptyList(), 0, new TypeSchema());
        JavaClass class2 = new JavaClass(new JavaBasicType("bar.MyClass"), new JavaBasicType("java.lang.Object"), emptyList(), 0, new TypeSchema());

        ClassMetadataCollection classes = new ClassMetadataCollection();
        classes.addClass(class1);
        classes.addClass(class2);
        JacksonTypeTranslator translator = new JacksonTypeTranslator(settings, classes, new TypeSchema(), new ApiDefinition());

        translator.translateType(class1.getType());
        translator.translateType(class2.getType());
    }

    @Test
    public void classHierarchyWithIgnores() {
        ClassDefinition classDefinition = translateClass(TypeWithIgnoresAndSuperClass.class);

        assertThat(classDefinition.getProperties(), hasProperties(
                property("bar", ApiPrimitiveType.STRING),
                property("baz", ApiPrimitiveType.STRING)));
    }

    @Test
    public void interfaceWithProperties() {
        ClassDefinition classDefinition = translateClass(TypeWithPropertiesFromInterface.class);

        assertThat(classDefinition.getProperties(), hasProperties(
                property("foo", ApiPrimitiveType.STRING),
                property("bar", ApiPrimitiveType.STRING)));
    }

    @Test
    public void enumTranslation() {
        EnumDefinition enumDefinition = translateEnum(TestEnum.class);

        assertThat(enumDefinition.getConstants(), is(asList("FOO", "BAR", "BAZ")));
    }

    private ApiType translateType(JavaType type) {
        ClassMetadataCollection classes = new ClassMetadataCollection();
        ApiDefinition api = new ApiDefinition();
        JacksonTypeTranslator translator = new JacksonTypeTranslator(settings, classes, new TypeSchema(), api);

        return translator.translateType(type);
    }

    private ClassDefinition translateClass(Class<?> cl) {
        ApiDefinition api = new ApiDefinition();
        ApiType apiType = translateClass(cl, api);

        Optional<ClassDefinition> definition = api.getClassDefinitions().stream().filter(d -> d.getType().toString().equals(apiType.typeRepresentation())).findAny();
        return definition.orElseThrow(() -> new AssertionError("could not find definition for " + apiType));
    }

    private EnumDefinition translateEnum(Class<?> cl) {
        ApiDefinition api = new ApiDefinition();
        ApiType apiType = translateClass(cl, api);

        Optional<EnumDefinition> definition = api.getEnumDefinitions().stream().filter(d -> d.getType().toString().equals(apiType.typeRepresentation())).findAny();
        return definition.orElseThrow(() -> new AssertionError("could not find definition for " + apiType));
    }

    private ApiType translateClass(Class<?> cl, ApiDefinition api) {
        ClassMetadataCollection classes = loadClassesFromInheritanceTree(cl);
        JacksonTypeTranslator translator = new JacksonTypeTranslator(settings, classes, new TypeSchema(), api);

        return translator.translateType(new JavaBasicType(cl));
    }

    @SuppressWarnings("rawtypes")
    public static final class ClassWithFieldProperties {

        public int intField;
        public Integer integerField;
        public String stringField;
        public boolean booleanField;
        public Boolean booleanNonPrimitiveField;
        public int[] intArrayField;
        public Collection<String> stringCollectionField;
        public Collection rawCollectionField;
        public Map<String,Integer> stringIntegerMapField;
        public Map rawMapField;
        public Object objectField;
    }

    public static final class ClassWithOptionalTypes {
        public Optional<String> optionalString;
        public OptionalInt optionalInt;
        public OptionalLong optionalLong;
        public OptionalDouble optionalDouble;
    }

    public static final class ClassWithJsonValue {
        public String bar;
        public String baz;

        @JsonValue
        public String foo() {
            return "foo";
        }
    }

    @SuppressWarnings("unused")
    public static final class ClassWithGetters {

        public int getInt() {
            throw new UnsupportedOperationException();
        }

        public Integer getInteger() {
            throw new UnsupportedOperationException();
        }

        public String getString() {
            throw new UnsupportedOperationException();
        }
        public boolean isBoolean() {
            throw new UnsupportedOperationException();
        }

        public Boolean getBooleanNonPrimitive() {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unused")
    public static final class TypeWithOverlappingFieldAndGetter {

        public String foo;

        public String getFoo() {
            return foo;
        }
    }

    public static class SuperClass {
        public String foo;
        public String bar;

        @JsonIgnore
        public String baz;
        public String quux;

        @JsonIgnore
        public String getFoo() {
            return foo;
        }
    }

    public static final class TypeWithIgnoresAndSuperClass extends SuperClass {
        public String baz;
        @JsonIgnore
        public String quux;

        @Override
        public String getFoo() {
            return foo;
        }
    }

    public interface ParentInterfaceWithProperties {
        default String getFoo() {
            return "foo";
        }
    }

    public interface ChildInterfaceWithProperties extends ParentInterfaceWithProperties {
        default String getBar() {
            return "bar";
        }
    }

    public static final class TypeWithPropertiesFromInterface implements ChildInterfaceWithProperties {
    }

    public enum TestEnum {
        FOO, BAR, BAZ
    }
}
