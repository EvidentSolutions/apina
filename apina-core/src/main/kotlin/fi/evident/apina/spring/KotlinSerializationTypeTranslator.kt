package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.model.*
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import kotlinx.metadata.*
import kotlinx.metadata.jvm.fieldSignature
import kotlinx.metadata.jvm.syntheticMethodForAnnotations
import java.lang.UnsupportedOperationException

/**
 * Translates kotlinx-serialization classes to model types.
 */
internal class KotlinSerializationTypeTranslator(
    private val typeTranslator: TypeTranslator,
    private val classes: JavaModel,
    private val api: ApiDefinition
) {

    private val discriminatorProperty = "type"

    fun supports(javaClass: JavaClass) =
        javaClass.hasAnnotation(SERIALIZABLE)

    fun translateClass(javaClass: JavaClass, typeName: ApiTypeName, env: TypeEnvironment): ApiType {
        val classType = ApiType.Class(typeName)

        val metadata = javaClass.kotlinMetadata ?: error("Could not find Kotlin metadata for class ${javaClass.name}")

        if (!api.containsType(typeName)) {
            when {
                javaClass.isEnum ->
                    api.addEnumDefinition(EnumDefinition(typeName, javaClass.enumConstants))

                Flag.IS_SEALED(metadata.flags) ->
                    createDiscriminatedUnion(javaClass, metadata)

                else -> {
                    val classDefinition = ClassDefinition(typeName)
                    // Add this before further processing because the type may be recursively referenced in definition
                    api.addClassDefinition(classDefinition)
                    initClassDefinition(classDefinition, javaClass, env)
                }
            }
        }

        return classType
    }

    private fun createDiscriminatedUnion(javaClass: JavaClass, metadata: KmClass) {
        val union = DiscriminatedUnionDefinition(
            typeTranslator.classNameForType(javaClass.type.toBasicType()),
            discriminatorProperty
        )

        // Add this before further processing because the type may be recursively referenced inside subclasses
        api.addDiscriminatedUnion(union)

        // We assume that the class is sealed and therefore the subtypes must be in the same package
        for (cl in findSealedSubClasses(metadata)) {
            val name = cl.findAnnotation(SERIAL_NAME)?.getAttribute("value") ?: cl.name.replace('$', '.')
            val def = ClassDefinition(typeTranslator.classNameForType(cl.type.toBasicType()))
            initClassDefinition(def, cl, TypeEnvironment.empty())
            union.addType(name, def)
        }
    }

    private fun findSealedSubClasses(metadata: KmClass) =
        metadata.sealedSubclasses.mapNotNull { classes.findClass(kotlinNameToJavaName(it)) }

    private fun kotlinNameToJavaName(name: String) =
        name.replace('.', '$').replace('/', '.')


    private fun initClassDefinition(
        classDefinition: ClassDefinition,
        javaClass: JavaClass,
        env: TypeEnvironment
    ) {

        val classesUpwardsFrom = classes.classesUpwardsFrom(BoundClass(javaClass, env))
        for (cl in classesUpwardsFrom) {
            val metadata = cl.javaClass.kotlinMetadata ?: continue
            val primaryConstructor = metadata.constructors.find { !Flag.Constructor.IS_SECONDARY(it.flags) } ?: continue

            for (property in metadata.properties.filter { it.fieldSignature != null }) {
                val parameter = primaryConstructor.valueParameters.find { it.name == property.name }
                processProperty(
                    cl.javaClass, cl.environment, property, classDefinition,
                    hasInitializer = parameter == null || Flag.ValueParameter.DECLARES_DEFAULT_VALUE(parameter.flags),
                )
            }
        }
    }

    private fun processProperty(
        javaClass: JavaClass,
        env: TypeEnvironment,
        property: KmProperty,
        classDefinition: ClassDefinition,
        hasInitializer: Boolean
    ) {
        val javaType = resolveJavaType(property.returnType)

        val annotationSource = property.syntheticMethodForAnnotations
            ?.let { p -> javaClass.methods.find { it.name == p.name && it.descriptor == p.desc } }

        if (annotationSource?.hasAnnotation(TRANSIENT) == true)
            return

        val propertyName = annotationSource?.findAnnotation(SERIAL_NAME)?.getAttribute("value") ?: property.name

        var type = typeTranslator.translateType(javaType, env)
        if (hasInitializer && annotationSource?.findAnnotation(REQUIRED) == null)
            type = type.nullable() // TODO: strictly speaking these are undefined and not null

        classDefinition.addProperty(PropertyDefinition(propertyName, type))
    }

    private fun resolveJavaType(type: KmType): JavaType = when (val classifier = type.classifier) {
        is KmClassifier.Class -> resolveJavaTypeForKotlinClassName(classifier.name)
        is KmClassifier.TypeAlias -> throw UnsupportedOperationException("can't resolve Java-types for type-alias: ${classifier.name}'")
        is KmClassifier.TypeParameter -> throw UnsupportedOperationException("can't resolve Java-types for type-parameter: '${classifier.id}'")
    }

    private fun resolveJavaTypeForKotlinClassName(name: String): JavaType = when (name) {
        "kotlin/Boolean" -> JavaType.Basic.BOOLEAN
        "kotlin/Int" -> JavaType.Basic.INT
        "kotlin/Short" -> JavaType.Basic.SHORT
        "kotlin/Long" -> JavaType.Basic.LONG
        "kotlin/Float" -> JavaType.Basic.FLOAT
        "kotlin/Double" -> JavaType.Basic.DOUBLE
        "kotlin/Unit" -> JavaType.Basic.VOID
        "kotlin/String" -> JavaType.Basic(String::class.java)
        else -> JavaType.Basic(kotlinNameToJavaName(name))
    }

    companion object {

        private val SERIALIZABLE = JavaType.Basic("kotlinx.serialization.Serializable")
        private val TRANSIENT = JavaType.Basic("kotlinx.serialization.Transient")
        private val SERIAL_NAME = JavaType.Basic("kotlinx.serialization.SerialName")
        private val REQUIRED = JavaType.Basic("kotlinx.serialization.Required")
    }
}
