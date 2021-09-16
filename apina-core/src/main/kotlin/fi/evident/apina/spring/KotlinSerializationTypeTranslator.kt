package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.model.*
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmValueParameter

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

        val metadata = javaClass.kotlinMetadata ?: error("no kotlin metadata for ${javaClass.name}")

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
                    initClassDefinition(classDefinition, javaClass, metadata, env)
                }
            }
        }

        return classType
    }

    private fun createDiscriminatedUnion(javaClass: JavaClass, metadata: KmClass) {
        val union = DiscriminatedUnionDefinition(typeTranslator.classNameForType(javaClass.type.toBasicType()), discriminatorProperty)

        // Add this before further processing because the type may be recursively referenced inside subclasses
        api.addDiscriminatedUnion(union)

        // We assume that the class is sealed and therefore the subtypes must be in the same package
        for (cl in findSealedSubClasses(metadata)) {
            val name = cl.findAnnotation(SERIAL_NAME)?.getAttribute("value") ?: cl.name.replace('$', '.')
            val def = ClassDefinition(typeTranslator.classNameForType(cl.type.toBasicType()))
            initClassDefinition(def, cl, cl.kotlinMetadata ?: error("no kotlin metadata for ${cl.name}"), TypeEnvironment.empty())
            union.addType(name, def)
        }
    }

    private fun findSealedSubClasses(metadata: KmClass) =
        metadata.sealedSubclasses.mapNotNull { classes.findClass(kotlinNameToJavaName(it)) }

    private fun kotlinNameToJavaName(name: String) =
        name.replace('.', '$').replace('/', '.')

    private fun initClassDefinition(classDefinition: ClassDefinition,
                                    javaClass: JavaClass,
                                    metadata: KmClass,
                                    env: TypeEnvironment) {

        val primary = metadata.constructors.find { !Flag.Constructor.IS_SECONDARY(it.flags) }
            ?: error("Could not find primary constructor for ${javaClass.name}")

        for (parameter in primary.valueParameters)
            processProperty(javaClass, env, parameter, classDefinition)
    }

    private fun processProperty(
        javaClass: JavaClass,
        env: TypeEnvironment,
        parameter: KmValueParameter,
        classDefinition: ClassDefinition
    ) {
        val getter = javaClass.getters.find { it.propertyName == parameter.name }
            ?: error("Cold not find getter for property ${parameter.name} of ${javaClass.name}")

        val annotationSource = javaClass.findExtraAnnotationSource(getter)
        if (annotationSource?.hasAnnotation(TRANSIENT) == true)
            return

        val propertyName = annotationSource?.findAnnotation(SERIAL_NAME)?.getAttribute("value") ?: parameter.name

        var type = typeTranslator.translateType(getter.returnType, getter, env)
        if (Flag.ValueParameter.DECLARES_DEFAULT_VALUE(parameter.flags) && annotationSource?.findAnnotation(REQUIRED) == null)
            type = type.nullable() // TODO: strictly speaking these are undefined and not null

        classDefinition.addProperty(PropertyDefinition(propertyName, type))
    }

    companion object {

        private val SERIALIZABLE = JavaType.Basic("kotlinx.serialization.Serializable")
        private val TRANSIENT = JavaType.Basic("kotlinx.serialization.Transient")
        private val SERIAL_NAME = JavaType.Basic("kotlinx.serialization.SerialName")
        private val REQUIRED = JavaType.Basic("kotlinx.serialization.Required")
    }
}
