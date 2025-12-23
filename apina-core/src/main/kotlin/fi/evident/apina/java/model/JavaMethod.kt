package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.model.type.TypeSchema
import fi.evident.apina.utils.propertyNameForGetter
import kotlin.metadata.KmFunction
import kotlin.metadata.jvm.signature
import java.lang.reflect.Modifier
import kotlin.metadata.KmType
import kotlin.metadata.isSuspend

class JavaMethod(
    val descriptor: String,
    val owningClass: JavaClass,
    val name: String,
    private val visibility: JavaVisibility,
    val returnType: JavaType,
    val parameters: List<JavaParameter>,
    private val modifiers: Int,
    val schema: TypeSchema
) : JavaAnnotatedElement {

    private val _annotations = ArrayList<JavaAnnotation>()

    private val kotlinMetadata: KmFunction? by lazy {
        owningClass.kotlinMetadata?.functions?.find {
            it.signature?.name == name && it.signature?.descriptor == descriptor
        }
    }

    val propertyName: String
        get() = propertyNameForGetter(name)

    val correspondingField: JavaField?
        get() = owningClass.findField(propertyName)

    override val annotations: List<JavaAnnotation>
        get() = _annotations

    val isPublic: Boolean
        get() = visibility == JavaVisibility.PUBLIC

    val isGetter: Boolean
        get() {
            if (isStatic || !parameters.isEmpty()) return false

            return name.startsWith("get") || (name.startsWith("is") && returnType == JavaType.Basic.BOOLEAN)
        }

    val environment: TypeEnvironment
        get() = TypeEnvironment(owningClass.schema, schema)

    val isStatic: Boolean
        get() = Modifier.isStatic(modifiers)

    val kotlinReturnType: KmType?
        get() = kotlinMetadata?.returnType

    fun addAnnotation(annotation: JavaAnnotation) {
        _annotations += annotation
    }

    override fun toString() = "$visibility $returnType $name ${parameters.joinToString(",", "(", ")")}"
}
