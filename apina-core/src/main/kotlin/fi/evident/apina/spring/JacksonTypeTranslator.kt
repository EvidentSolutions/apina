package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotatedElement
import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.JavaModel
import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.model.ApiDefinition
import fi.evident.apina.model.ClassDefinition
import fi.evident.apina.model.EnumDefinition
import fi.evident.apina.model.PropertyDefinition
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.utils.propertyNameForGetter
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Translates Java types to model types.
 */
internal class JacksonTypeTranslator(private val settings: TranslationSettings,
                                     private val classes: JavaModel,
                                     private val api: ApiDefinition) {

    /**
     * Maps translated simple names back to their original types.
     * Needed to make sure that our mapping remains unique.
     */
    private val translatedNames = HashMap<String, JavaType.Basic>()

    fun translateType(javaType: JavaType, element: JavaAnnotatedElement, env: TypeEnvironment): ApiType {
        val type = translateType(javaType, env)
        return if (element.hasAnnotation(NULLABLE))
            ApiType.Nullable(type)
        else
            type
    }

    private fun translateType(type: JavaType, env: TypeEnvironment): ApiType = when (type) {
        is JavaType.Basic ->
            translateBasicType(type, env)
        is JavaType.Parameterized ->
            translateParameterizedType(type, env)
        is JavaType.Array ->
            ApiType.Array(translateType(type.elementType, env))
        is JavaType.Variable ->
            env.lookup(type)?.let {
                check(it != type) { "Looking up type returned itself: $type in $env" }
                translateType(it, env)
            } ?: ApiType.Primitive.ANY
        is JavaType.Wildcard ->
            type.lowerBound?.let { translateType(it, env) } ?: ApiType.Primitive.ANY
        is JavaType.InnerClass ->
            throw UnsupportedOperationException("translating inner class types is not supported: $type")
    }

    private fun translateBasicType(type: JavaType.Basic, env: TypeEnvironment): ApiType = when {
        classes.isInstanceOf<Collection<*>>(type) ->
            ApiType.Array(ApiType.Primitive.ANY)

        classes.isInstanceOf<Map<*,*>>(type) ->
            ApiType.Dictionary(ApiType.Primitive.ANY)

        type == JavaType.Basic(String::class.java) ->
            ApiType.Primitive.STRING

        classes.isNumber(type) ->
            ApiType.Primitive.NUMBER

        type == JavaType.Basic.BOOLEAN || type == JavaType.Basic(Boolean::class.javaObjectType) ->
            ApiType.Primitive.BOOLEAN

        type in OPTIONAL_NUMBER_TYPES ->
            ApiType.Nullable(ApiType.Primitive.NUMBER)

        type == JavaType.Basic(Any::class.java) ->
            ApiType.Primitive.ANY

        type.isVoid ->
            ApiType.Primitive.VOID

        else ->
            translateClassType(type, env)
    }

    private fun translateParameterizedType(type: JavaType.Parameterized, env: TypeEnvironment): ApiType {
        val baseType = type.baseType
        val arguments = type.arguments

        return when {
            classes.isInstanceOf<Collection<*>>(baseType) && arguments.size == 1 ->
                ApiType.Array(translateType(arguments[0], env))
            classes.isInstanceOf<Map<*,*>>(baseType) && arguments.size == 2 && classes.isInstanceOf<String>(arguments[0]) ->
                ApiType.Dictionary(translateType(arguments[1], env))
            classes.isInstanceOf<Optional<*>>(baseType) && arguments.size == 1 ->
                ApiType.Nullable(translateType(arguments[0], env))
            else -> {
                arguments.filterIsInstance<JavaType.Basic>().forEach{
                    val typeName = classNameForType(it)
                    if (!settings.isImported(typeName) && !settings.isBlackBoxClass(it.name) && !hasJsonValueAnnotation(it) ) {
                        addDefinitionIfNotExistsYet(it, typeName, env)
                    }
                }
                return translateType(baseType, env)
            }
        } // TODO: use arguments
    }

    private fun translateClassType(type: JavaType.Basic, env: TypeEnvironment): ApiType {
        val typeName = classNameForType(type)

        if (settings.isImported(typeName))
            return ApiType.BlackBox(typeName)

        if (settings.isBlackBoxClass(type.name) || hasJsonValueAnnotation(type)) {
            log.debug("Translating {} as black box", type.name)

            val blackBoxType = ApiType.BlackBox(typeName)
            api.addBlackBox(typeName)
            return blackBoxType
        }

        val classType = ApiType.Class(typeName)

        addDefinitionIfNotExistsYet(type, typeName, env)

        return classType
    }

    private fun addDefinitionIfNotExistsYet(type: JavaType.Basic, typeName: ApiTypeName, env: TypeEnvironment) {
        if (!api.containsType(typeName)) {
            val aClass = classes.findClass(type.name)
            if (aClass != null) {
                if (aClass.isEnum) {
                    api.addEnumDefinition(EnumDefinition(typeName, aClass.enumConstants))

                } else {
                    val classDefinition = ClassDefinition(typeName)

                    // We must first add the definition to api and only then proceed to
                    // initialize it because initialization of properties could refer
                    // back to this same class and we'd get infinite recursion if the
                    // class is not already installed.
                    api.addClassDefinition(classDefinition)
                    initClassDefinition(classDefinition, BoundClass(aClass, env))
                }
            }
        }
    }

    private fun classNameForType(type: JavaType.Basic): ApiTypeName {
        val translatedName = translateClassName(type.name)

        val existingType = translatedNames.putIfAbsent(translatedName, type)
        if (existingType != null && type != existingType)
            throw DuplicateClassNameException(type.name, existingType.name)

        return ApiTypeName(translatedName)
    }

    private fun hasJsonValueAnnotation(type: JavaType.Basic) =
            classes.findClass(type.name)?.hasMethodWithAnnotation(JSON_VALUE) ?: false

    private fun initClassDefinition(classDefinition: ClassDefinition, boundClass: BoundClass) {
        val ignoredProperties = ignoredProperties(boundClass)

        val acceptProperty = { name: String -> !classDefinition.hasProperty(name) && !ignoredProperties.contains(name) }

        for (cl in classesUpwardsFrom(boundClass)) {
            addPropertiesFromGetters(cl, classDefinition, acceptProperty)
            addPropertiesFromFields(cl, classDefinition, acceptProperty)
        }
    }

    private fun addPropertiesFromFields(boundClass: BoundClass, classDefinition: ClassDefinition, acceptProperty: (String) -> Boolean) {
        for (field in boundClass.javaClass.publicInstanceFields) {
            val name = field.name
            if (acceptProperty(name)) {
                val type = translateType(field.type, field, boundClass.environment)
                classDefinition.addProperty(PropertyDefinition(name, type))
            }
        }
    }

    private fun addPropertiesFromGetters(boundClass: BoundClass, classDefinition: ClassDefinition, acceptProperty: (String) -> Boolean) {
        for (getter in boundClass.javaClass.getters) {
            val name = propertyNameForGetter(getter.name)
            if (acceptProperty(name)) {
                val type = translateType(getter.returnType, getter, boundClass.environment)
                classDefinition.addProperty(PropertyDefinition(name, type))
            }
        }
    }

    private fun ignoredProperties(type: BoundClass): Set<String> {
        val ignores = HashSet<String>()

        val classes = classesUpwardsFrom(type)

        for (i in classes.indices.reversed()) {
            val aClass = classes[i].javaClass

            for (field in aClass.publicInstanceFields) {
                if (field.findAnnotation(JSON_IGNORE)?.isIgnore() == true)
                    ignores.add(field.name)
                else
                    ignores.remove(field.name)
            }

            for (getter in aClass.getters) {
                val ignore = getter.findAnnotation(JSON_IGNORE)
                if (ignore != null) {
                    val name = propertyNameForGetter(getter.name)
                    if (ignore.isIgnore()) {
                        ignores.add(name)
                    } else {
                        ignores.remove(name)
                    }
                }
            }
        }

        return ignores
    }

    private fun classesUpwardsFrom(javaClass: BoundClass): List<BoundClass> {
        val result = ArrayList<BoundClass>()

        fun recurse(c: BoundClass) {
            if (result.any { c.javaClass == it.javaClass })
                return

            result += c

            c.javaClass.interfaces
                    .asSequence()
                    .mapNotNull { boundClassFor(it, c.environment) }
                    .forEach(::recurse)
        }

        var cl: BoundClass? = javaClass
        while (cl != null) {
            recurse(cl)
            cl = boundClassFor(cl.javaClass.superClass, cl.environment)
        }

        return result
    }

    private fun boundClassFor(type: JavaType, env: TypeEnvironment): BoundClass? =
            classes.findClass(type.nonGenericClassName)?.let { c ->
                if (type is JavaType.Parameterized)
                    BoundClass(c, c.schema.apply(env.resolve(type.arguments)))
                else
                    BoundClass(c, TypeEnvironment.empty())
            }

    companion object {

        private val log = LoggerFactory.getLogger(JacksonTypeTranslator::class.java)

        private val NULLABLE = JavaType.Basic("org.jetbrains.annotations.Nullable")
        private val JSON_IGNORE = JavaType.Basic("com.fasterxml.jackson.annotation.JsonIgnore")
        private val JSON_VALUE = JavaType.Basic("com.fasterxml.jackson.annotation.JsonValue")
        private val OPTIONAL_NUMBER_TYPES = listOf(JavaType.basic<OptionalInt>(), JavaType.basic<OptionalLong>(), JavaType.basic<OptionalDouble>())

        private fun JavaAnnotation.isIgnore() = getAttribute("value") ?: true
    }
}
