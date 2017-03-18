package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaClass
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.model.type.TypeSchema
import fi.evident.apina.java.reader.loadClassesFromInheritanceTree
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.EnumDefinition
import fi.evident.apina.model.ModelMatchers.hasProperties
import fi.evident.apina.model.ModelMatchers.property
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.spring.testclasses.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.Collections.emptyList
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JacksonTypeTranslatorTest {

    private val settings = TranslationSettings()

    @Test
    fun translatingClassWithFieldProperties() {
        val classDefinition = translateClass<ClassWithFieldProperties>()

        assertEquals(ApiTypeName(ClassWithFieldProperties::class.java.simpleName), classDefinition.type)
        assertThat(classDefinition.properties, hasProperties(
                property("intField", ApiType.Primitive.NUMBER),
                property("integerField", ApiType.Primitive.NUMBER),
                property("stringField", ApiType.Primitive.STRING),
                property("booleanField", ApiType.Primitive.BOOLEAN),
                property("booleanNonPrimitiveField", ApiType.Primitive.BOOLEAN),
                property("intArrayField", ApiType.Array(ApiType.Primitive.NUMBER)),
                property("rawCollectionField", ApiType.Array(ApiType.Primitive.ANY)),
                property("wildcardMapField", ApiType.Dictionary(ApiType.Primitive.ANY)),
                property("rawMapField", ApiType.Dictionary(ApiType.Primitive.ANY)),
                property("stringIntegerMapField", ApiType.Dictionary(ApiType.Primitive.NUMBER)),
                property("objectField", ApiType.Primitive.ANY),
                property("stringCollectionField", ApiType.Array(ApiType.Primitive.STRING))))
    }

    @Test
    fun translatingClassWithGetterProperties() {
        val classDefinition = translateClass<ClassWithGetters>()

        assertEquals(ApiTypeName(ClassWithGetters::class.java.simpleName), classDefinition.type)
        assertThat(classDefinition.properties, hasProperties(
                property("int", ApiType.Primitive.NUMBER),
                property("integer", ApiType.Primitive.NUMBER),
                property("string", ApiType.Primitive.STRING),
                property("boolean", ApiType.Primitive.BOOLEAN),
                property("booleanNonPrimitive", ApiType.Primitive.BOOLEAN)))
    }

    @Test
    fun translatingClassWithOverlappingFieldAndGetter() {
        val classDefinition = translateClass<TypeWithOverlappingFieldAndGetter>()

        assertThat(classDefinition.properties, hasProperties(
                property("foo", ApiType.Primitive.STRING)))
    }

    @Test
    fun translateVoidType() {
        assertEquals(ApiType.Primitive.VOID, translateType(JavaType.Basic.VOID))
    }

    @Test
    fun translateBlackBoxType() {
        settings.blackBoxClasses.addPattern("foo\\..+")

        assertEquals(ApiType.BlackBox(ApiTypeName("Baz")), translateType(JavaType.Basic("foo.bar.Baz")))
    }

    @Test
    fun translatingOptionalTypes() {
        val classDefinition = translateClass<ClassWithOptionalTypes>()

        assertEquals(ApiTypeName(ClassWithOptionalTypes::class.java.simpleName), classDefinition.type)
        assertThat(classDefinition.properties, hasProperties(
                property("optionalString", ApiType.Nullable(ApiType.Primitive.STRING)),
                property("optionalInt", ApiType.Nullable(ApiType.Primitive.NUMBER)),
                property("optionalLong", ApiType.Nullable(ApiType.Primitive.NUMBER)),
                property("optionalDouble", ApiType.Nullable(ApiType.Primitive.NUMBER))))
    }

    @Test
    fun typesWithJsonValueShouldBeBlackBoxes() {
        val apiType = translateClass<ClassWithJsonValue>(ApiDefinition())

        assertEquals(ApiType.BlackBox(ApiTypeName("ClassWithJsonValue")), apiType)
    }

    @Test
    fun duplicateClassNames() {
        val class1 = JavaClass(JavaType.Basic("foo.MyClass"), JavaType.Basic("java.lang.Object"), emptyList(), 0, TypeSchema())
        val class2 = JavaClass(JavaType.Basic("bar.MyClass"), JavaType.Basic("java.lang.Object"), emptyList(), 0, TypeSchema())

        val classes = JavaModel()
        classes.addClass(class1)
        classes.addClass(class2)
        val translator = JacksonTypeTranslator(settings, classes, ApiDefinition())
        val env = TypeEnvironment.empty()

        translator.translateType(class1.type, class1, env)

        assertFailsWith<DuplicateClassNameException> {
            translator.translateType(class2.type, class2, env)
        }
    }

    @Test
    fun classHierarchyWithIgnores() {
        val classDefinition = translateClass<TypeWithIgnoresAndSuperClass>()

        assertThat(classDefinition.properties, hasProperties(
                property("bar", ApiType.Primitive.STRING),
                property("baz", ApiType.Primitive.STRING)))
    }

    @Test
    fun ignoresOverriddenInSubClasses() {
        val classDefinition = translateClass<TypeWithOverridingIgnore>()

        assertThat(classDefinition.properties, hasProperties(
                property("foo", ApiType.Primitive.STRING)))
    }

    @Test
    fun interfaceWithProperties() {
        val classDefinition = translateClass<TypeWithPropertiesFromInterface>()

        assertThat(classDefinition.properties, hasProperties(
                property("foo", ApiType.Primitive.STRING),
                property("bar", ApiType.Primitive.STRING)))
    }

    @Test
    fun enumTranslation() {
        val enumDefinition = translateEnum<TestEnum>()

        assertEquals(listOf("FOO", "BAR", "BAZ"), enumDefinition.constants)
    }

    @Test
    fun genericTypeWithoutKnownParameters() {
        val classDefinition = translateClass<GenericType<*>>()

        assertThat(classDefinition.properties, hasProperties(
                property("genericField", ApiType.Primitive.ANY)))
    }

    @Test
    fun genericTypeInheritedByFixingParameters() {
        val classDefinition = translateClass<SubTypeOfGenericType>()

        assertThat(classDefinition.properties, hasProperties(
                property("genericField", ApiType.Primitive.STRING)))
    }

    @Test
    fun genericTypeInheritedThroughMiddleType() {
        val classDefinition = translateClass<SecondOrderSubTypeOfGenericType>()

        assertThat(classDefinition.properties, hasProperties(
                property("genericField", ApiType.Primitive.STRING)))
    }

    @Test
    fun genericTypeInheritedThroughMiddleTypeThatParameterizesWithVariable() {
        val classDefinition = translateClass<SubTypeOfGenericTypeParameterizedWithVariable>()

        assertThat(classDefinition.properties, hasProperties(
                property("genericField", ApiType.Array(ApiType.Primitive.STRING))))
    }


    @Test
    fun unboundTypeVariable() {
        @Suppress("unused")
        abstract class Bar<A>
        class Foo<B> : Bar<B>()

        assertEquals("Foo", translateClass<Foo<*>>().type.name)
    }

    interface GenericSuperType<out T> {
        @Suppress("unused")
        fun get(): T
    }

    interface GenericSubType<out T> : GenericSuperType<T>

    @Test
    fun shadowedTypesShouldNotPreventTranslation() {
        translateClass<GenericSubType<String>>()
    }

    private fun translateType(type: JavaType): ApiType {
        val classes = JavaModel()
        val api = ApiDefinition()
        val translator = JacksonTypeTranslator(settings, classes, api)

        return translator.translateType(type, MockAnnotatedElement(), TypeEnvironment.empty()) // TODO: create environment from type
    }

    private inline fun <reified T : Any> translateClass(): ClassDefinition {
        val api = ApiDefinition()
        val apiType = translateClass<T>(api)

        return api.classDefinitions.find { d -> apiType.typeRepresentation().startsWith(d.type.toString()) }
            ?: throw AssertionError("could not find definition for $apiType")
    }

    private inline fun <reified T : Enum<T>> translateEnum(): EnumDefinition {
        val api = ApiDefinition()
        val apiType = translateClass<T>(api)

        return api.enumDefinitions.find { d -> d.type.toString() == apiType.typeRepresentation() }
            ?: throw AssertionError("could not find definition for $apiType")
    }

    private inline fun <reified T : Any> translateClass(api: ApiDefinition): ApiType {
        val model = JavaModel()
        model.loadClassesFromInheritanceTree<T>()
        val translator = JacksonTypeTranslator(settings, model, api)

        return translator.translateType(JavaType.basic<T>(), MockAnnotatedElement(), TypeEnvironment.empty())
    }
}
