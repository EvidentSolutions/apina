package fi.evident.apina.spring

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import fi.evident.apina.java.model.JavaClass
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.model.type.TypeSchema
import fi.evident.apina.java.reader.TestClassMetadataLoader
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.EnumDefinition
import fi.evident.apina.model.ModelMatchers.hasProperties
import fi.evident.apina.model.ModelMatchers.property
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.spring.testclasses.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList
import java.util.Collections.singletonMap
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JacksonTypeTranslatorTest {

    private val settings = TranslationSettings()

    @Test
    fun translatingClassWithFieldProperties() {
        val classDefinition = translateClass<ClassWithFieldProperties>()

        assertEquals(ApiTypeName(ClassWithFieldProperties::class.java.simpleName), classDefinition.type)
        assertThat(classDefinition.properties, hasProperties(
                property("intField", ApiType.Primitive.INTEGER),
                property("floatField", ApiType.Primitive.FLOAT),
                property("doubleField", ApiType.Primitive.FLOAT),
                property("integerField", ApiType.Primitive.INTEGER),
                property("stringField", ApiType.Primitive.STRING),
                property("booleanField", ApiType.Primitive.BOOLEAN),
                property("booleanNonPrimitiveField", ApiType.Primitive.BOOLEAN),
                property("intArrayField", ApiType.Array(ApiType.Primitive.INTEGER)),
                property("rawCollectionField", ApiType.Array(ApiType.Primitive.ANY)),
                property("wildcardMapField", ApiType.Dictionary(ApiType.Primitive.ANY)),
                property("rawMapField", ApiType.Dictionary(ApiType.Primitive.ANY)),
                property("stringIntegerMapField", ApiType.Dictionary(ApiType.Primitive.INTEGER)),
                property("objectField", ApiType.Primitive.ANY),
                property("stringCollectionField", ApiType.Array(ApiType.Primitive.STRING))))
    }

    @Test
    fun translatingClassWithGetterProperties() {
        val classDefinition = translateClass<ClassWithGetters>()

        assertEquals(ApiTypeName(ClassWithGetters::class.java.simpleName), classDefinition.type)
        assertThat(classDefinition.properties, hasProperties(
                property("int", ApiType.Primitive.INTEGER),
                property("integer", ApiType.Primitive.INTEGER),
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
                property("optionalInt", ApiType.Nullable(ApiType.Primitive.INTEGER)),
                property("optionalLong", ApiType.Nullable(ApiType.Primitive.INTEGER)),
                property("optionalDouble", ApiType.Nullable(ApiType.Primitive.FLOAT))))
    }

    @Test
    fun `types with @JsonValue should be translated as aliased types`() {
        val api = ApiDefinition()
        val apiType = translateClass<ClassWithJsonValue>(api)

        assertEquals(ApiType.BlackBox(ApiTypeName("ClassWithJsonValue")), apiType)
        assertEquals(singletonMap(ApiTypeName("ClassWithJsonValue"), ApiType.Primitive.STRING), api.typeAliases)
    }

    @Test
    fun duplicateClassNames() {
        val class1 = JavaClass(JavaType.Basic("foo.MyClass"), JavaType.Basic("java.lang.Object"), emptyList(), 0, TypeSchema())
        val class2 = JavaClass(JavaType.Basic("bar.MyClass"), JavaType.Basic("java.lang.Object"), emptyList(), 0, TypeSchema())

        val loader = TestClassMetadataLoader()
        loader.addClass(class1)
        loader.addClass(class2)
        val classes = JavaModel(loader)
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
    fun `generic type parameters are translated for unknown generic types`() {
        class Foo
        class Bar
        @Suppress("unused") class GenericType<T, S>

        class Root {
            @Suppress("unused")
            lateinit var foo: GenericType<Foo, Bar>
        }

        val model = JavaModel(TestClassMetadataLoader().apply {
            loadClassesFromInheritanceTree<Root>()
            loadClassesFromInheritanceTree<GenericType<*, *>>()
            loadClassesFromInheritanceTree<Foo>()
            loadClassesFromInheritanceTree<Bar>()
        })
        val api = ApiDefinition()
        val translator = JacksonTypeTranslator(settings, model, api)
        translator.translateType(JavaType.basic<Root>(), MockAnnotatedElement(), TypeEnvironment.empty())

        assertTrue(api.classDefinitions.any { it.type.name == "Foo" }, "Class definition for Foo is created")
        assertTrue(api.classDefinitions.any { it.type.name == "Bar" }, "Class definition for Bar is created")
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

    @Test
    fun `translating discriminated unions`() {
        val model = JavaModel(TestClassMetadataLoader().apply {
            loadClassesFromInheritanceTree<Vehicle>()
            loadClassesFromInheritanceTree<Vehicle.Car>()
            loadClassesFromInheritanceTree<Vehicle.Truck>()
        })

        val api = ApiDefinition()
        val translator = JacksonTypeTranslator(settings, model, api)
        translator.translateType(JavaType.basic<Vehicle>(), MockAnnotatedElement(), TypeEnvironment.empty())

        assertEquals(1, api.discriminatedUnionDefinitions.size)
        val definition = api.discriminatedUnionDefinitions.first()
        assertEquals("Vehicle", definition.type.name)
        assertEquals("type", definition.discriminator)

        val types = definition.types
        assertEquals(2, types.size)
        assertEquals(setOf("car", "truck"), types.keys)
        assertEquals("Car", types["car"]?.toTypeScript())
        assertEquals("Truck", types["truck"]?.toTypeScript())

        // ensure subclasses themselves are also translated
        assertEquals(2, api.classDefinitions.size)
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Vehicle.Car::class, name = "car"),
        JsonSubTypes.Type(value = Vehicle.Truck::class, name = "truck")
    )
    abstract class Vehicle {
        class Car : Vehicle()
        class Truck : Vehicle()
    }

    private fun translateType(type: JavaType): ApiType {
        val classes = JavaModel(TestClassMetadataLoader())
        val api = ApiDefinition()
        val translator = JacksonTypeTranslator(settings, classes, api)

        return translator.translateType(type, MockAnnotatedElement(), TypeEnvironment.empty()) // TODO: create environment from type
    }

    private inline fun <reified T : Any> translateClass(): ClassDefinition {
        val api = ApiDefinition()
        val apiType = translateClass<T>(api)

        return api.classDefinitions.find { d -> apiType.toTypeScript().startsWith(d.type.toString()) }
            ?: throw AssertionError("could not find definition for $apiType")
    }

    private inline fun <reified T : Enum<T>> translateEnum(): EnumDefinition {
        val api = ApiDefinition()
        val apiType = translateClass<T>(api)

        return api.enumDefinitions.find { d -> d.type.toString() == apiType.toTypeScript() }
            ?: throw AssertionError("could not find definition for $apiType")
    }

    private inline fun <reified T : Any> translateClass(api: ApiDefinition): ApiType {
        val model = JavaModel(TestClassMetadataLoader().apply {
            loadClassesFromInheritanceTree<T>()
        })
        val translator = JacksonTypeTranslator(settings, model, api)

        return translator.translateType(JavaType.basic<T>(), MockAnnotatedElement(), TypeEnvironment.empty())
    }
}
