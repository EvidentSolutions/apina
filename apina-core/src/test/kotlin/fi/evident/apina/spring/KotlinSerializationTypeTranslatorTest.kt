@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.reader.TestClassMetadataLoader
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.spring.testclasses.*
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class KotlinSerializationTypeTranslatorTest {

    private val api = ApiDefinition()
    private val settings = TranslationSettings()
    private val loader = TestClassMetadataLoader()
    private val model = JavaModel(loader)
    private val translator = TypeTranslator(settings, model, api)

    @Test
    fun `basic class definition`() {

        @Suppress("unused")
        @Serializable
        class Example(
            val normalProperty: String,
            @Transient val ignoredProperty: String = "",
            @SerialName("overriddenName") val propertyWithOverriddenName: String,
            val fieldWithDefaultWillBeNullable: Int = 42,
            @Required val requiredFieldWithDefaultWillNotBeNullable: Int = 42,
            val nullableParameter: Int? = null
        )

        val classDefinition = translateClass<Example>()

        assertHasProperties(
            classDefinition,
            "normalProperty" to ApiType.Primitive.STRING,
            "overriddenName" to ApiType.Primitive.STRING,
            "fieldWithDefaultWillBeNullable" to ApiType.Primitive.INTEGER.nullable(),
            "nullableParameter" to ApiType.Primitive.INTEGER.nullable(),
            "requiredFieldWithDefaultWillNotBeNullable" to ApiType.Primitive.INTEGER
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
        @Serializable
        open class ParentClass(val parentParameter: Int) {
            var parentProperty = "string"

            @Required
            var requiredParentProperty = "string"

            val propertyWithoutBackingField: String
                get() = "no-included"
        }

        @Serializable
        class ChildClass(val ownParameter: Int) : ParentClass(42) {
            var ownProperty = "string"

            @Required
            var requiredOwnProperty = "string"

            @Transient
            private var transientPrivateProperty = "42"

            @SerialName("renamedPrivatePropertyNewName")
            private var renamedPrivateProperty = "42"

            private var privateProperty = "42"

            @Required
            private var requiredPrivateProperty = "42"

            @Required
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

    @Test
    fun `translate kotlin type types to Java types`() {

        @Serializable
        @Suppress("unused")
        class MyClass(
            val stringProperty: String,
            val booleanProperty: Boolean,
            val intProperty: Int,
            val shortProperty: Short,
            val longProperty: Long,
            val floatProperty: Float,
            val doubleProperty: Double,
        )

        val classDefinition = translateClass<MyClass>()
        assertHasProperties(
            classDefinition,
            "stringProperty" to ApiType.Primitive.STRING,
            "booleanProperty" to ApiType.Primitive.BOOLEAN,
            "intProperty" to ApiType.Primitive.INTEGER,
            "shortProperty" to ApiType.Primitive.INTEGER,
            "longProperty" to ApiType.Primitive.INTEGER,
            "floatProperty" to ApiType.Primitive.FLOAT,
            "doubleProperty" to ApiType.Primitive.FLOAT,
        )
    }

    @Test
    fun `check that inline classses are translated as type-aliases`() {
        @Serializable
        @Suppress("unused")
        class MyClass(
            val integerProperty: Int,
            val integerValueTypeProperty: ValueInteger
        )

        loader.loadClassesFromInheritanceTree<ValueInteger>()

        val classDefinition = translateClass<MyClass>()
        assertHasProperties(
            classDefinition,
            "integerProperty" to ApiType.Primitive.INTEGER,
            "integerValueTypeProperty" to ApiType.BlackBox(ApiTypeName("ValueInteger"))
        )

        assertHasTypeAlias(api, "ValueInteger", ApiType.Primitive.INTEGER)
    }

    @Serializable
    sealed class KotlinSerializationDiscriminatedUnion {

        @Serializable
        class SubClassWithDefaultDiscriminator(val x: Int) : KotlinSerializationDiscriminatedUnion()

        @Serializable
        @SerialName("CustomDiscriminator")
        class SubClassWithCustomDiscriminator(val y: Int) : KotlinSerializationDiscriminatedUnion()
    }

    @Test
    fun `translating nullable types`() {
        @Serializable
        @Suppress("unused")
        class MyClass(
            val nullableIntegerProperty: Int?,
        )

        val classDefinition = translateClass<MyClass>()

        assertHasProperties(
            classDefinition,
            "nullableIntegerProperty" to ApiType.Nullable(ApiType.Primitive.INTEGER),
        )
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
        val apiType = translateType<T>()

        return api.classDefinitions.find { d ->
            apiType.toTypeScript(OptionalTypeMode.NULL).startsWith(d.type.toString())
        } ?: fail("could not find definition for $apiType")
    }
}
