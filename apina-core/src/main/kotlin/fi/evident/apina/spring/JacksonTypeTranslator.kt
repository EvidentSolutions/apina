package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.model.*
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
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
        return if (element.hasNullableAnnotation)
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

        classes.isIntegral(type) ->
            ApiType.Primitive.INTEGER

        classes.isNumber(type) ->
            ApiType.Primitive.FLOAT

        type == JavaType.Basic.BOOLEAN || type == JavaType.Basic(Boolean::class.javaObjectType) ->
            ApiType.Primitive.BOOLEAN

        type in OPTIONAL_INTEGRAL_TYPES ->
            ApiType.Nullable(ApiType.Primitive.INTEGER)

        type == OPTIONAL_DOUBLE ->
            ApiType.Nullable(ApiType.Primitive.FLOAT)

        type == JavaType.Basic(Any::class.java) ->
            ApiType.Primitive.ANY

        type.isVoid ->
            ApiType.Primitive.VOID

        else ->
            translateClassType(type, env)
    }

    private fun translateParameterizedType(type: JavaType.Parameterized, env: TypeEnvironment): ApiType {
        val baseType = type.baseType
        val arguments = type.arguments.map { translateType(it, env) }

        return when {
            classes.isInstanceOf<Collection<*>>(baseType) && arguments.size == 1 ->
                ApiType.Array(arguments[0])
            classes.isInstanceOf<Map<*,*>>(baseType) && arguments.size == 2 && arguments[0] == ApiType.Primitive.STRING ->
                ApiType.Dictionary(arguments[1])
            classes.isInstanceOf<Optional<*>>(baseType) && arguments.size == 1 ->
                ApiType.Nullable(arguments[0])
            else ->
                translateType(baseType, env)
        }
    }

    private fun translateClassType(type: JavaType.Basic, env: TypeEnvironment): ApiType {
        val typeName = classNameForType(type)

        if (settings.isImported(typeName))
            return ApiType.BlackBox(typeName)

        if (settings.isBlackBoxClass(type.name)) {
            log.debug("Translating {} as black box", type.name)

            api.addBlackBox(typeName)
            return ApiType.BlackBox(typeName)
        }

        val jsonValueMethod = classes.findClass(type.name)?.findMethodWithAnnotation(JSON_VALUE)
        if (jsonValueMethod != null) {
            api.addTypeAlias(typeName, translateType(jsonValueMethod.returnType, env))
            return ApiType.BlackBox(typeName)
        }

        val classType = ApiType.Class(typeName)

        if (!api.containsType(typeName)) {
            val aClass = classes.findClass(type.name)
            if (aClass != null) {
                when {
                    aClass.isEnum ->
                        api.addEnumDefinition(EnumDefinition(typeName, aClass.enumConstants))
                    aClass.hasAnnotation(JSON_TYPE_INFO) -> {
                        val typeInfo = aClass.findAnnotation(JSON_TYPE_INFO) ?: error("@JsonTypeInfo missing for $aClass")
                        createDiscriminatedUnion(type, typeInfo, aClass)
                    }
                    else -> {
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

        return classType
    }

    private fun createDiscriminatedUnion(javaType: JavaType.Basic, typeInfo: JavaAnnotation, javaClass: JavaClass) {
        val use = typeInfo.getRequiredAttribute<EnumValue>("use").constant
        val include = typeInfo.getAttribute<EnumValue>("include")?.constant ?: "PROPERTY"
        val property = typeInfo.getAttribute("property") ?: ""

        check(use == "NAME") { "Only 'use=NAME' is supported for @JsonTypeInfo (in $javaType)" }
        check(include == "PROPERTY") { "Only 'include=PROPERTY' is supported for @JsonTypeInfo (in $javaType)" }
        check(property.isNotEmpty()) { "No 'property' defined for @JsonTypeInfo (in $javaType)" }

        val union = DiscriminatedUnionDefinition(classNameForType(javaType), property)

        // Add this before further processing because the type may be recursively referenced inside subclasses
        api.addDiscriminatedUnion(union)

        for ((name, cl) in findSubtypes(javaClass))
            union.addType(name, translateType(cl, TypeEnvironment.empty()))
    }

    private fun findSubtypes(javaClass: JavaClass): List<Pair<String, JavaType>> {
        val subTypes = javaClass.findAnnotation(JSON_SUB_TYPES)
        if (subTypes != null) {
            return subTypes.getAttributeValues("value").map { subType ->
                val annotation = subType as JavaAnnotation
                val value = annotation.getRequiredAttribute<JavaType>("value")
                val name = annotation.getAttribute<String>("name")
                    ?: error("no name defined subclass $value (in ${javaClass.type})")
                name to value
            }
        } else {
            // If there's no @JsonSubTypes annotation, we can still do the same thing as Jackson does and detect
            // subtypes of sealed class automatically. We could scan Kotlin's metadata, but there's actually a simpler
            // way: we know that sealed classes must reside in the same package as the original class since they must
            // be defined in the same source file and one source file can only output classes in one package. Therefore
            // we can just scan the classes in the same package as the original class.
            return classes.findDirectSubclassesInSamePackage(javaClass).map { cl ->
                val typeName = cl.findAnnotation(JSON_TYPE_NAME)
                    ?: error("No @JsonTypeName annotation found for ${cl.name}. Either specify @JsonTypeName or use @JsonSubTypes at ${javaClass.name}")
                val name = typeName.getRequiredAttribute<String>("value")
                name to cl.type
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

    private fun initClassDefinition(classDefinition: ClassDefinition, boundClass: BoundClass, prefix: String = "", suffix: String = "") {
        val ignoredProperties = ignoredProperties(boundClass)

        val acceptProperty = { name: String -> !classDefinition.hasProperty(name) && name !in ignoredProperties }

        for (cl in classes.classesUpwardsFrom(boundClass)) {
            for (getter in cl.javaClass.getters)
                processProperty(cl, classDefinition, getter.propertyName, getter, getter.returnType, acceptProperty, prefix, suffix)
            for (field in cl.javaClass.publicInstanceFields)
                processProperty(cl, classDefinition, field.name, field, field.type, acceptProperty, prefix, suffix)
        }
    }

    private fun processProperty(boundClass: BoundClass,
                                classDefinition: ClassDefinition,
                                name: String,
                                element: JavaAnnotatedElement,
                                javaType: JavaType,
                                acceptProperty: (String) -> Boolean,
                                prefix: String,
                                suffix: String) {
        val unwrappedAnnotation = element.findAnnotation(JSON_UNWRAPPED)
        if (unwrappedAnnotation != null && unwrappedAnnotation.getAttribute("enabled", Boolean::class.java) != false) {
            val newPrefix = unwrappedAnnotation.getAttribute("prefix", String::class.java) ?: ""
            val newSuffix = unwrappedAnnotation.getAttribute("suffix", String::class.java) ?: ""
            val type = classes.findClass(javaType.toBasicType())
            if (type != null)
                initClassDefinition(classDefinition, BoundClass(type, TypeEnvironment.empty()), prefix = newPrefix + prefix, suffix = suffix + newSuffix)

        } else if (acceptProperty(name)) {
            val type = translateType(javaType, element, boundClass.environment)
            classDefinition.addProperty(PropertyDefinition(prefix + name + suffix, type))
        }
    }

    private fun ignoredProperties(type: BoundClass): Set<String> {
        val ignores = HashSet<String>()

        val classes = classes.classesUpwardsFrom(type)

        for (i in classes.indices.reversed()) {
            val aClass = classes[i].javaClass

            for (field in aClass.publicInstanceFields) {
                if (field.findAnnotation(JSON_IGNORE)?.isIgnore() == true || field.isTransient || field.hasExternalIgnoreAnnotation())
                    ignores.add(field.name)
                else
                    ignores.remove(field.name)
            }

            for (getter in aClass.getters) {
                val ignore = getter.findAnnotation(JSON_IGNORE) ?: getter.correspondingField?.findAnnotation(JSON_IGNORE)

                if (ignore != null) {
                    if (ignore.isIgnore()) {
                        ignores.add(getter.propertyName)
                    } else {
                        ignores.remove(getter.propertyName)
                    }
                } else if (getter.hasExternalIgnoreAnnotation() || getter.correspondingField?.hasExternalIgnoreAnnotation() == true) {
                    ignores.add(getter.propertyName)
                }
            }
        }

        return ignores
    }

    private fun JavaAnnotatedElement.hasExternalIgnoreAnnotation() =
        hasAnnotation(JAVA_BEANS_TRANSIENT) || hasAnnotation(SPRING_DATA_TRANSIENT)

    companion object {

        private val log = LoggerFactory.getLogger(JacksonTypeTranslator::class.java)

        private val JSON_IGNORE = JavaType.Basic("com.fasterxml.jackson.annotation.JsonIgnore")
        private val JSON_VALUE = JavaType.Basic("com.fasterxml.jackson.annotation.JsonValue")
        private val JSON_TYPE_INFO = JavaType.Basic("com.fasterxml.jackson.annotation.JsonTypeInfo")
        private val JSON_TYPE_NAME = JavaType.Basic("com.fasterxml.jackson.annotation.JsonTypeName")
        private val JSON_SUB_TYPES = JavaType.Basic("com.fasterxml.jackson.annotation.JsonSubTypes")
        private val JSON_UNWRAPPED = JavaType.Basic("com.fasterxml.jackson.annotation.JsonUnwrapped")
        private val JAVA_BEANS_TRANSIENT = JavaType.Basic(java.beans.Transient::class)
        private val SPRING_DATA_TRANSIENT = JavaType.Basic("org.springframework.data.annotation.Transient")
        private val OPTIONAL_INTEGRAL_TYPES = listOf(JavaType.basic<OptionalInt>(), JavaType.basic<OptionalLong>())
        private val OPTIONAL_DOUBLE = JavaType.basic<OptionalDouble>()

        private fun JavaAnnotation.isIgnore() = getAttribute("value") ?: true
    }
}
