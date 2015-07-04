package fi.evident.apina.spring;

import fi.evident.apina.java.model.ClassMetadataCollection;
import fi.evident.apina.java.model.JavaClass;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;
import fi.evident.apina.java.reader.ClassReaderUtils;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.ClassDefinition;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.*;
import org.junit.Test;

import java.util.*;

import static fi.evident.apina.model.ModelMatchers.hasProperties;
import static fi.evident.apina.model.ModelMatchers.property;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JacksonTypeTranslatorTest {

    private final TranslationSettings settings = new TranslationSettings();

    @Test
    public void translatingClassWithFieldProperties() {
        ClassDefinition classDefinition = translateClass(ClassWithFieldProperties.class);

        assertThat(classDefinition.getType(), is(new ApiClassType(ClassWithFieldProperties.class.getSimpleName())));
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

        assertThat(classDefinition.getType(), is(new ApiClassType(ClassWithGetters.class.getSimpleName())));
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

        assertThat(translateType(new JavaBasicType("foo.bar.Baz")), is(new ApiBlackBoxType("Baz")));
    }

    @Test
    public void translatingOptionalTypes() {
        ClassDefinition classDefinition = translateClass(ClassWithOptionalTypes.class);

        assertThat(classDefinition.getType(), is(new ApiClassType(ClassWithOptionalTypes.class.getSimpleName())));
        assertThat(classDefinition.getProperties(), hasProperties(
                property("optionalString", ApiPrimitiveType.STRING),
                property("optionalInt", ApiPrimitiveType.NUMBER),
                property("optionalLong", ApiPrimitiveType.NUMBER),
                property("optionalDouble", ApiPrimitiveType.NUMBER)));
    }

    private ApiType translateType(JavaType type) {
        ClassMetadataCollection classes = new ClassMetadataCollection(emptyList());
        ApiDefinition api = new ApiDefinition();
        JacksonTypeTranslator translator = new JacksonTypeTranslator(settings, classes, new TypeSchema(), api);

        return translator.translateType(type);
    }

    private ClassDefinition translateClass(Class<?> cl) {
        JavaClass javaClass = ClassReaderUtils.loadClass(cl);

        ClassMetadataCollection classes = new ClassMetadataCollection(singleton(javaClass));
        ApiDefinition api = new ApiDefinition();
        JacksonTypeTranslator translator = new JacksonTypeTranslator(settings, classes, new TypeSchema(), api);

        ApiType apiType = translator.translateType(javaClass.getType());

        Optional<ClassDefinition> definition = api.getClassDefinitions().stream().filter(d -> d.getType().equals(apiType)).findAny();
        return definition.orElseThrow(() -> new AssertionError("could not find definition for " + apiType));
    }

    @SuppressWarnings({"unused", "rawtypes"})
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
}
