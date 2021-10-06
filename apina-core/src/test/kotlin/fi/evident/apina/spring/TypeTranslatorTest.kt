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
import fi.evident.apina.spring.testclasses.*
import kotlinx.serialization.SerialName
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

        assertEquals(ApiType.BlackBox(ApiTypeName("Baz")),
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

            assertEquals(setOf("bar", "baz"), classDefinition.propertyNames)
        }

        @Test
        fun `ignores can be overridden in sub classes`() {
            val classDefinition = translateClass<TypeWithOverridingIgnore>()

            assertEquals(setOf("foo"), classDefinition.propertyNames)
        }
    }

    @Test
    fun `ignore properties annotated with java beans Transient`() {
        val classDefinition = translateClass<ClassWithTransientIgnore>()

        assertEquals(setOf("bar"), classDefinition.propertyNames)
    }

    @Test
    fun `ignore properties annotated with Spring Data Transient`() {
        val classDefinition = translateClass<ClassWithSpringDataTransient>()

        assertEquals(setOf("foo"), classDefinition.propertyNames)
    }

    @Test
    fun `ignore transient fields`() {
        val classDefinition = translateClass<ClassWithTransientFields>()

        assertEquals(setOf("foo", "baz"), classDefinition.propertyNames)
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

    @Nested
    inner class `kotlin serialization` {

        @Test
        fun `basic class definition`() {

            @Suppress("unused")
            @kotlinx.serialization.Serializable
            class Example(
                val normalProperty: String,
                @kotlinx.serialization.Transient val ignoredProperty: String = "",
                @kotlinx.serialization.SerialName("overriddenName") val propertyWithOverriddenName: String,
                val fieldWithDefaultWillBeNullable: Int = 42,
                @kotlinx.serialization.Required val requiredFieldWithDefaultWillNotBeNullable: Int = 42,
                val nullableParameter: Int?,
                val valueString: ValueString
            )

            val classDefinition = translateClass<Example>()

            assertHasProperties(
                classDefinition,
                "normalProperty" to ApiType.Primitive.STRING,
                "overriddenName" to ApiType.Primitive.STRING,
                "fieldWithDefaultWillBeNullable" to ApiType.Primitive.INTEGER.nullable(),
                "nullableParameter" to ApiType.Primitive.INTEGER.nullable(),
                "requiredFieldWithDefaultWillNotBeNullable" to ApiType.Primitive.INTEGER,
                "valueString" to ApiType.Primitive.STRING
            )
        }

        @Test
        fun `discriminated unions`() {
            loader.loadClassesFromInheritanceTree<KotlinSerializationDiscriminatedUnion>()
            loader.loadClassesFromInheritanceTree<KotlinSerializationDiscriminatedUnion.SubClassWithCustomDiscriminator>()
            loader.loadClassesFromInheritanceTree<KotlinSerializationDiscriminatedUnion.SubClassWithDefaultDiscriminator>()

            translator.translateType(
                JavaType.basic<KotlinSerializationDiscriminatedUnion>(),
                MockAnnotatedElement(),
                TypeEnvironment.empty()
            )

            val definition =
                api.discriminatedUnionDefinitions.find { it.type.name == KotlinSerializationDiscriminatedUnion::class.simpleName }
                    ?: fail("could not find union")

            assertEquals("type", definition.discriminator)

            assertEquals(
                setOf(
                    "CustomDiscriminator",
                    KotlinSerializationDiscriminatedUnion.SubClassWithDefaultDiscriminator::class.qualifiedName
                ),
                definition.types.keys
            )
        }

        @Suppress("unused")
        @Test
        fun `inherited fields`() {
            @kotlinx.serialization.Serializable
            open class ParentClass(val parentParameter: Int) {
                var parentProperty = "string"

                @kotlinx.serialization.Required
                var requiredParentProperty = "string"

                val propertyWithoutBackingField: String
                    get() = "no-included"
            }

            @kotlinx.serialization.Serializable
            class ChildClass(val ownParameter: Int) : ParentClass(42) {
                var ownProperty = "string"

                @kotlinx.serialization.Required
                var requiredOwnProperty = "string"

                @kotlinx.serialization.Transient
                private var transientPrivateProperty = "42"

                @SerialName("renamedPrivatePropertyNewName")
                private var renamedPrivateProperty = "42"

                private var privateProperty = "42"

                @kotlinx.serialization.Required
                private var requiredPrivateProperty = "42"

                @kotlinx.serialization.Required
                private var isProperty = false
            }

            loader.loadClassesFromInheritanceTree<ParentClass>()
            loader.loadClassesFromInheritanceTree<ChildClass>()

            translator.translateType(JavaType.basic<ChildClass>(), MockAnnotatedElement(), TypeEnvironment.empty())

            val classDefinition = api.classDefinitions.find { it.type.name == ChildClass::class.simpleName }
                ?: fail("could not find class")

            assertHasProperties(
                classDefinition,
                "ownParameter" to ApiType.Primitive.INTEGER,
                "ownProperty" to ApiType.Primitive.STRING.nullable(),
                "requiredOwnProperty" to ApiType.Primitive.STRING,
                "privateProperty" to ApiType.Primitive.STRING.nullable(),
                "renamedPrivatePropertyNewName" to ApiType.Primitive.STRING.nullable(),
                "requiredPrivateProperty" to ApiType.Primitive.STRING,
                "isProperty" to ApiType.Primitive.BOOLEAN,
                "parentParameter" to ApiType.Primitive.INTEGER,
                "parentProperty" to ApiType.Primitive.STRING.nullable(),
                "requiredParentProperty" to ApiType.Primitive.STRING
            )
        }
    }

    @kotlinx.serialization.Serializable
    sealed class KotlinSerializationDiscriminatedUnion {

        @kotlinx.serialization.Serializable
        class SubClassWithDefaultDiscriminator(val x: Int) : KotlinSerializationDiscriminatedUnion()

        @kotlinx.serialization.Serializable
        @kotlinx.serialization.SerialName("CustomDiscriminator")
        class SubClassWithCustomDiscriminator(val y: Int) : KotlinSerializationDiscriminatedUnion()
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
        translator.translateType(type, MockAnnotatedElement(), TypeEnvironment.empty()) // TODO: create environment from type

    private inline fun <reified T : Any> translateType(): ApiType =
        translateType(JavaType.basic<T>())

    private inline fun <reified T : Any> translateClass(): ClassDefinition {
        loader.loadClassesFromInheritanceTree<T>()
        val apiType = translateType<T>()

        return api.classDefinitions.find { d ->
            apiType.toTypeScript(OptionalTypeMode.NULL).startsWith(d.type.toString())
        } ?: fail("could not find definition for $apiType")
    }

    private inline fun <reified T : Enum<T>> translateEnum(): EnumDefinition {
        loader.loadClassesFromInheritanceTree<T>()
        val apiType = translateType<T>()

        return api.enumDefinitions.find { d -> d.type.toString() == apiType.toTypeScript(OptionalTypeMode.NULL) }
            ?: fail("could not find definition for $apiType")
    }

    companion object {

        private fun assertHasProperties(classDefinition: ClassDefinition, vararg properties: Pair<String, ApiType>) {
            assertEquals(
                classDefinition.properties.associate { it.name to it.type }.toSortedMap(),
                properties.toMap().toSortedMap(),
                "Properties don't match"
            )
        }

        private val ClassDefinition.propertyNames: Set<String>
            get() = properties.map { it.name }.toSet()
    }
}
