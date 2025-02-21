package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.model.*
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName

/**
 * Translates Jackson classes to model types.
 */
internal class JacksonTypeTranslator(
    private val typeTranslator: TypeTranslator,
    private val classes: JavaModel,
    private val api: ApiDefinition
) {

    fun translateClass(javaClass: JavaClass, typeName: ApiTypeName, env: TypeEnvironment): ApiType {
        val jsonValueMethod = javaClass.findMethodWithAnnotation(JSON_VALUE)
        if (jsonValueMethod != null) {
            api.addTypeAlias(typeName, typeTranslator.translateType(jsonValueMethod.returnType, env))
            return ApiType.BlackBox(typeName)
        }

        val classType = ApiType.Class(typeName)

        if (!api.containsType(typeName)) {
            when {
                javaClass.isEnum ->
                    api.addEnumDefinition(EnumDefinition(typeName, javaClass.enumConstants))
                javaClass.hasAnnotation(JSON_TYPE_INFO) -> {
                    val typeInfo =
                        javaClass.findAnnotation(JSON_TYPE_INFO) ?: error("@JsonTypeInfo missing for $javaClass")
                    createDiscriminatedUnion(javaClass, typeInfo)
                }
                else -> {
                    val classDefinition = ClassDefinition(typeName)

                    // We must first add the definition to api and only then proceed to
                    // initialize it because initialization of properties could refer
                    // back to this same class and we'd get infinite recursion if the
                    // class is not already installed.
                    api.addClassDefinition(classDefinition)
                    initClassDefinition(classDefinition, BoundClass(javaClass, env))
                }
            }
        }

        return classType
    }

    private fun createDiscriminatedUnion(javaClass: JavaClass, typeInfo: JavaAnnotation) {
        val use = typeInfo.getRequiredAttribute<EnumValue>("use").constant
        val include = typeInfo.getAttribute<EnumValue>("include")?.constant ?: "PROPERTY"
        val property = typeInfo.getAttribute("property") ?: ""

        check(use == "NAME") { "Only 'use=NAME' is supported for @JsonTypeInfo (in ${javaClass.name})" }
        check(include == "PROPERTY") { "Only 'include=PROPERTY' is supported for @JsonTypeInfo (in ${javaClass.name})" }
        check(property.isNotEmpty()) { "No 'property' defined for @JsonTypeInfo (in ${javaClass.name})" }

        val union = DiscriminatedUnionDefinition(typeTranslator.classNameForType(javaClass.type.toBasicType()), property)

        // Add this before further processing because the type may be recursively referenced inside subclasses
        api.addDiscriminatedUnion(union)

        for ((name, cl) in findSubtypes(javaClass)) {
            val def = ClassDefinition(typeTranslator.classNameForType(cl.type.toBasicType()))
            initClassDefinition(def, BoundClass(cl, TypeEnvironment.empty()))
            union.addType(name, def)
        }
    }

    private fun findSubtypes(javaClass: JavaClass): List<Pair<String, JavaClass>> {
        val subTypes = javaClass.findAnnotation(JSON_SUB_TYPES)
        if (subTypes != null) {
            return subTypes.getAttributeValues("value").map { subType ->
                val annotation = subType as JavaAnnotation
                val value = annotation.getRequiredAttribute<JavaType>("value")
                val name = annotation.getAttribute<String>("name")
                    ?: error("no name defined subclass $value (in ${javaClass.type})")
                val cl = classes.findClass(value.toBasicType()) ?: error("could not find class $value")
                name to cl
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
                name to cl
            }
        }
    }

    private fun initClassDefinition(
        classDefinition: ClassDefinition,
        boundClass: BoundClass,
        prefix: String = "",
        suffix: String = ""
    ) {
        val ignoredProperties = ignoredProperties(boundClass)

        val acceptProperty = { name: String -> !classDefinition.hasProperty(name) && name !in ignoredProperties }

        for (cl in classes.classesUpwardsFrom(boundClass)) {
            if (cl.javaClass.isRecord) {
                for (component in cl.javaClass.recordComponents)
                    processProperty(cl, classDefinition, component.name, component, component.type, acceptProperty, prefix, suffix)
            } else {
                for (getter in cl.javaClass.getters)
                    processProperty(cl, classDefinition, getter.propertyName, getter, getter.returnType, acceptProperty, prefix, suffix)
                for (field in cl.javaClass.publicInstanceFields)
                    processProperty(cl, classDefinition, field.name, field, field.type, acceptProperty, prefix, suffix)
            }
        }
    }

    private fun processProperty(
        boundClass: BoundClass,
        classDefinition: ClassDefinition,
        name: String,
        element: JavaAnnotatedElement,
        javaType: JavaType,
        acceptProperty: (String) -> Boolean,
        prefix: String,
        suffix: String
    ) {
        val unwrappedAnnotation = element.findAnnotation(JSON_UNWRAPPED)
        if (unwrappedAnnotation != null && unwrappedAnnotation.getAttribute("enabled", Boolean::class.java) != false) {
            val newPrefix = unwrappedAnnotation.getAttribute("prefix", String::class.java) ?: ""
            val newSuffix = unwrappedAnnotation.getAttribute("suffix", String::class.java) ?: ""
            val type = classes.findClass(javaType.toBasicType())
            if (type != null)
                initClassDefinition(
                    classDefinition,
                    BoundClass(type, TypeEnvironment.empty()),
                    prefix = newPrefix + prefix,
                    suffix = suffix + newSuffix
                )

        } else if (acceptProperty(name)) {
            val type = typeTranslator.translateType(javaType, element, boundClass.environment)
            classDefinition.addProperty(PropertyDefinition(prefix + name + suffix, type))
        }
    }

    private fun ignoredProperties(type: BoundClass): Set<String> {
        val ignores = HashSet<String>()

        val classes = classes.classesUpwardsFrom(type)

        for (i in classes.indices.reversed()) {
            val aClass = classes[i].javaClass

            for (field in aClass.publicInstanceFields) {
                if (field.findAnnotation(JSON_IGNORE)
                        ?.isIgnore() == true || field.isTransient || field.hasExternalIgnoreAnnotation()
                )
                    ignores.add(field.name)
                else
                    ignores.remove(field.name)
            }

            for (getter in aClass.getters) {
                val ignore =
                    getter.findAnnotation(JSON_IGNORE) ?: getter.correspondingField?.findAnnotation(JSON_IGNORE)

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

        private val JSON_IGNORE = JavaType.Basic("com.fasterxml.jackson.annotation.JsonIgnore")
        private val JSON_VALUE = JavaType.Basic("com.fasterxml.jackson.annotation.JsonValue")
        private val JSON_TYPE_INFO = JavaType.Basic("com.fasterxml.jackson.annotation.JsonTypeInfo")
        private val JSON_TYPE_NAME = JavaType.Basic("com.fasterxml.jackson.annotation.JsonTypeName")
        private val JSON_SUB_TYPES = JavaType.Basic("com.fasterxml.jackson.annotation.JsonSubTypes")
        private val JSON_UNWRAPPED = JavaType.Basic("com.fasterxml.jackson.annotation.JsonUnwrapped")
        private val JAVA_BEANS_TRANSIENT = JavaType.Basic(java.beans.Transient::class)
        private val SPRING_DATA_TRANSIENT = JavaType.Basic("org.springframework.data.annotation.Transient")

        private fun JavaAnnotation.isIgnore() = getAttribute("value") ?: true
    }
}
