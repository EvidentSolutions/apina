@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package fi.evident.apina.spring

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.annotation.JsonUnwrapped
import fi.evident.apina.java.model.JavaClass
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.model.type.TypeSchema
import fi.evident.apina.java.reader.TestClassMetadataLoader
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.EnumDefinition
import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.output.ts.toTypeScript
import fi.evident.apina.spring.testclasses.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList
import java.util.Collections.singletonMap
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

class TypeTranslatorTest {

    private val api = ApiDefinition()
    private val settings = TranslationSettings()
    private val loader = TestClassMetadataLoader()
    private val model = JavaModel(loader)
    private val translator = TypeTranslator(settings, model, api)

    @Test
    fun `translating class with value and inline classes`() {
        val classDefinition = translateClass<ClassWithValueClasses>()

        assertEquals(ApiTypeName(ClassWithValueClasses::class.java.simpleName), classDefinition.type)
        assertHasProperties(
            classDefinition,
            "valueString" to ApiType.Primitive.STRING,
            "valueInteger" to ApiType.Primitive.INTEGER,
            "nestedValueInteger" to ApiType.Primitive.INTEGER,
            "getterValue" to ApiType.Primitive.STRING
        )
    }

    @Test
    fun `translating class with field properties`() {
        val classDefinition = translateClass<ClassWithFieldProperties>()

        assertEquals(ApiTypeName(ClassWithFieldProperties::class.java.simpleName), classDefinition.type)
        assertHasProperties(
            classDefinition,
            "intField" to ApiType.Primitive.INTEGER,
            "floatField" to ApiType.Primitive.FLOAT,
            "doubleField" to ApiType.Primitive.FLOAT,
            "integerField" to ApiType.Primitive.INTEGER,
            "stringField" to ApiType.Primitive.STRING,
            "booleanField" to ApiType.Primitive.BOOLEAN,
            "booleanNonPrimitiveField" to ApiType.Primitive.BOOLEAN,
            "intArrayField" to ApiType.Array(ApiType.Primitive.INTEGER),
            "rawCollectionField" to ApiType.Array(ApiType.Primitive.ANY),
            "wildcardMapField" to ApiType.Dictionary(ApiType.Primitive.ANY),
            "rawMapField" to ApiType.Dictionary(ApiType.Primitive.ANY),
            "stringIntegerMapField" to ApiType.Dictionary(ApiType.Primitive.INTEGER),
            "objectField" to ApiType.Primitive.ANY,
            "stringCollectionField" to ApiType.Array(ApiType.Primitive.STRING)
        )
    }

    @Test
    fun `translating class with getter properties`() {
        val classDefinition = translateClass<ClassWithGetters>()

        assertEquals(ApiTypeName(ClassWithGetters::class.java.simpleName), classDefinition.type)
        assertHasProperties(
            classDefinition,
            "int" to ApiType.Primitive.INTEGER,
            "integer" to ApiType.Primitive.INTEGER,
            "string" to ApiType.Primitive.STRING,
            "boolean" to ApiType.Primitive.BOOLEAN,
            "booleanNonPrimitive" to ApiType.Primitive.BOOLEAN
        )
    }

    @Test
    fun `translating class with overlapping field and getter`() {
        val classDefinition = translateClass<TypeWithOverlappingFieldAndGetter>()

        assertHasProperties(classDefinition, "foo" to ApiType.Primitive.STRING)
    }

    @Test
    fun `translate void type`() {
        assertEquals(ApiType.Primitive.VOID, translateType(JavaType.Basic.VOID))
    }

    @Test
    fun `translate black-box type`() {
        settings.blackBoxClasses.addPattern("foo\\..+")

        assertEquals(
            ApiType.BlackBox(ApiTypeName("Baz")),
            translateType(JavaType.Basic("foo.bar.Baz"))
        )
    }

    @Test
    fun `translating optional types`() {
        val classDefinition = translateClass<ClassWithOptionalTypes>()

        assertEquals(ApiTypeName(ClassWithOptionalTypes::class.java.simpleName), classDefinition.type)
        assertHasProperties(
            classDefinition,
            "optionalString" to ApiType.Nullable(ApiType.Primitive.STRING),
            "optionalInt" to ApiType.Nullable(ApiType.Primitive.INTEGER),
            "optionalLong" to ApiType.Nullable(ApiType.Primitive.INTEGER),
            "optionalDouble" to ApiType.Nullable(ApiType.Primitive.FLOAT)
        )
    }

    @Test
    fun `types with @JsonValue should be translated as aliased types`() {
        loader.loadClassesFromInheritanceTree<ClassWithJsonValue>()
        val apiType = translateType<ClassWithJsonValue>()

        assertEquals(ApiType.BlackBox(ApiTypeName("ClassWithJsonValue")), apiType)
        assertEquals(singletonMap(ApiTypeName("ClassWithJsonValue"), ApiType.Primitive.STRING), api.typeAliases)
    }

    @Test
    fun `duplicate class names`() {
        val class1 =
            JavaClass(JavaType.Basic("foo.MyClass"), JavaType.Basic("java.lang.Object"), emptyList(), 0, TypeSchema())
        val class2 =
            JavaClass(JavaType.Basic("bar.MyClass"), JavaType.Basic("java.lang.Object"), emptyList(), 0, TypeSchema())

        loader.addClass(class1)
        loader.addClass(class2)
        val env = TypeEnvironment.empty()

        translator.translateType(class1.type, class1, env)

        assertFailsWith<DuplicateClassNameException> {
            translator.translateType(class2.type, class2, env)
        }
    }

    @Nested
    inner class `handling @JsonIgnore` {

        @Test
        fun `class hierarchy with ignores`() {
            val classDefinition = translateClass<TypeWithIgnoresAndSuperClass>()

            assertHasProperties(classDefinition, "bar", "baz")
        }

        @Test
        fun `ignores can be overridden in sub classes`() {
            val classDefinition = translateClass<TypeWithOverridingIgnore>()

            assertHasProperties(classDefinition, "foo")
        }
    }

    @Nested
    inner class `translating records` {
        @Test
        fun `simple records`() {
            val classDefinition = translateClass<SimpleRecord>()

            assertHasProperties(classDefinition, "foo", "bar")
        }
    }

    @Test
    fun `ignore properties annotated with java beans Transient`() {
        val classDefinition = translateClass<ClassWithTransientIgnore>()

        assertHasProperties(classDefinition, "bar")
    }

    @Test
    fun `ignore properties annotated with Spring Data Transient`() {
        val classDefinition = translateClass<ClassWithSpringDataTransient>()

        assertHasProperties(classDefinition, "foo")
    }

    @Test
    fun `ignore transient fields`() {
        val classDefinition = translateClass<ClassWithTransientFields>()

        assertHasProperties(classDefinition, "foo", "baz")
    }

    @Test
    fun `interface with properties`() {
        val classDefinition = translateClass<TypeWithPropertiesFromInterface>()

        assertHasProperties(
            classDefinition,
            "foo" to ApiType.Primitive.STRING,
            "bar" to ApiType.Primitive.STRING
        )
    }

    @Test
    fun `enum translation`() {
        val enumDefinition = translateEnum<TestEnum>()

        assertEquals(listOf("FOO", "BAR", "BAZ"), enumDefinition.constants)
    }

    @Test
    fun `generic type without known parameters`() {
        val classDefinition = translateClass<GenericType<*>>()

        assertHasProperties(classDefinition, "genericField" to ApiType.Primitive.ANY)
    }

    @Test
    fun `generic type inherited by fixing parameters`() {
        val classDefinition = translateClass<SubTypeOfGenericType>()

        assertHasProperties(classDefinition, "genericField" to ApiType.Primitive.STRING)
    }

    @Test
    fun `generic type inherited through middle type`() {
        val classDefinition = translateClass<SecondOrderSubTypeOfGenericType>()

        assertHasProperties(classDefinition, "genericField" to ApiType.Primitive.STRING)
    }

    @Test
    fun `generic type inherited through middle type that parameterizes with variable`() {
        val classDefinition = translateClass<SubTypeOfGenericTypeParameterizedWithVariable>()

        assertHasProperties(classDefinition, "genericField" to ApiType.Array(ApiType.Primitive.STRING))
    }

    @Test
    fun `generic type parameters are translated for unknown generic types`() {
        class Foo
        class Bar

        @Suppress("unused")
        class GenericType<T, S>

        class Root {
            @Suppress("unused")
            lateinit var foo: GenericType<Foo, Bar>
        }

        loader.loadClassesFromInheritanceTree<Root>()
        loader.loadClassesFromInheritanceTree<GenericType<*, *>>()
        loader.loadClassesFromInheritanceTree<Foo>()
        loader.loadClassesFromInheritanceTree<Bar>()
        translator.translateType(JavaType.basic<Root>(), MockAnnotatedElement(), TypeEnvironment.empty())

        assertTrue(api.classDefinitions.any { it.type.name == "Foo" }, "Class definition for Foo is created")
        assertTrue(api.classDefinitions.any { it.type.name == "Bar" }, "Class definition for Bar is created")
    }

    @Test
    fun `unbound type variable`() {
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
    fun `shadowed types should not prevent translation`() {
        translateClass<GenericSubType<String>>()
    }

    @Nested
    inner class `discriminated unions` {

        @Test
        fun `translating discriminated unions`() {
            loader.loadClassesFromInheritanceTree<Vehicle>()
            loader.loadClassesFromInheritanceTree<Vehicle.Car>()
            loader.loadClassesFromInheritanceTree<Vehicle.Truck>()

            translator.translateType(JavaType.basic<Vehicle>(), MockAnnotatedElement(), TypeEnvironment.empty())

            assertEquals(1, api.discriminatedUnionDefinitions.size)
            val definition = api.discriminatedUnionDefinitions.first()
            assertEquals("Vehicle", definition.type.name)
            assertEquals("type", definition.discriminator)

            val types = definition.types
            assertEquals(2, types.size)
            assertEquals(setOf("car", "truck"), types.keys)
            assertEquals("Car", types["car"]?.type?.name)
            assertEquals("Truck", types["truck"]?.type?.name)

            // ensure that subclasses themselves are not translated
            assertEquals(0, api.classDefinitions.size)
        }

        @Test
        fun `without @JsonSubtypes`() {
            loader.loadClassesFromInheritanceTree<Vehicle2>()
            loader.loadClassesFromInheritanceTree<Vehicle2.Car>()
            loader.loadClassesFromInheritanceTree<Vehicle2.Truck>()

            val translator = TypeTranslator(settings, model, api)
            translator.translateType(JavaType.basic<Vehicle2>(), MockAnnotatedElement(), TypeEnvironment.empty())

            assertEquals(1, api.discriminatedUnionDefinitions.size)
            val definition = api.discriminatedUnionDefinitions.first()
            assertEquals("Vehicle2", definition.type.name)
            assertEquals("type", definition.discriminator)

            val types = definition.types
            assertEquals(2, types.size)
            assertEquals(setOf("car", "truck"), types.keys)
            assertEquals("Car", types["car"]?.type?.name)
            assertEquals("Truck", types["truck"]?.type?.name)

            // ensure that subclasses themselves are not translated
            assertEquals(0, api.classDefinitions.size)
        }
    }

    @Test
    fun `translating unwrapped properties`() {

        @Suppress("unused")
        class Name(val first: String, val last: String)

        @Suppress("unused")
        class Person(@get:JsonUnwrapped val name: Name, val age: Int)

        loader.loadClassesFromInheritanceTree<Name>()
        loader.loadClassesFromInheritanceTree<Person>()

        translator.translateType(JavaType.basic<Person>(), MockAnnotatedElement(), TypeEnvironment.empty())

        assertEquals(1, api.classDefinitionCount)
        val person = api.classDefinitions.find { it.type.name == "Person" } ?: error("no Person found")
        assertEquals(setOf("age", "first", "last"), person.properties.map { it.name }.toSet())
    }

    @Test
    fun `translating unwrapped with prefixes and suffixes`() {

        @Suppress("unused")
        class Name(val first: String, val last: String)

        @Suppress("unused")
        class Person(
            @get:JsonUnwrapped(suffix = "Name") val name: Name,
            @get:JsonUnwrapped(prefix = "foo", suffix = "bar") val name2: Name,
            val age: Int
        )

        loader.loadClassesFromInheritanceTree<Name>()
        loader.loadClassesFromInheritanceTree<Person>()

        translator.translateType(JavaType.basic<Person>(), MockAnnotatedElement(), TypeEnvironment.empty())

        assertEquals(1, api.classDefinitionCount)
        val person = api.classDefinitions.find { it.type.name == "Person" } ?: error("no Person found")
        assertEquals(
            setOf("age", "firstName", "lastName", "foofirstbar", "foolastbar"),
            person.properties.map { it.name }.toSet()
        )
    }

    @Test
    fun `override translated class name`() {

        @Suppress("unused")
        class Foo(val foo: String)

        settings.nameTranslator.registerClassName(Foo::class.java.name, "MyOverriddenFoo")

        assertEquals("MyOverriddenFoo", translateClass<Foo>().type.name)
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

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    abstract class Vehicle2 {
        @JsonTypeName("car")
        class Car : Vehicle2()

        @JsonTypeName("truck")
        class Truck : Vehicle2()
    }

    private fun translateType(type: JavaType): ApiType =
        translator.translateType(
            type,
            MockAnnotatedElement(),
            TypeEnvironment.empty()
        ) // TODO: create environment from type

    private inline fun <reified T : Any> translateType(): ApiType =
        translateType(JavaType.basic<T>())

    private inline fun <reified T : Any> translateClass(): ClassDefinition {
        loader.loadClassesFromInheritanceTree<T>()
        val apiType = translateType<T>() as ApiType.Class

        return api.classDefinitions.find { it.type == apiType.name }
            ?: fail("could not find definition for $apiType")
    }

    private inline fun <reified T : Enum<T>> translateEnum(): EnumDefinition {
        loader.loadClassesFromInheritanceTree<T>()
        val apiType = translateType<T>() as ApiType.Class

        return api.enumDefinitions.find { it.type == apiType.name }
            ?: fail("could not find definition for $apiType")
    }
}
