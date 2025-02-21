package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.Metadata
import org.objectweb.asm.Opcodes
import java.lang.reflect.Modifier

/**
 * Contains all information read about class.
 */
class JavaClass(
    val type: JavaType,
    val superClass: JavaType,
    val interfaces: List<JavaType>,
    private val modifiers: Int,
    val schema: TypeSchema
) : JavaAnnotatedElement {

    private val _annotations = ArrayList<JavaAnnotation>()
    private val _fields = ArrayList<JavaField>()
    private val _methods = ArrayList<JavaMethod>()
    private val _recordComponent = ArrayList<JavaRecordComponent>()

    override val annotations: List<JavaAnnotation>
        get() = _annotations

    val methods: List<JavaMethod>
        get() = _methods

    val fields: List<JavaField>
        get() = _fields

    val recordComponents: List<JavaRecordComponent>
        get() = _recordComponent

    val isEnum: Boolean
        get() = modifiers and Opcodes.ACC_ENUM != 0

    val isRecord: Boolean
        get() = modifiers and Opcodes.ACC_RECORD != 0

    val isAnnotation: Boolean
        get() = isInterface && interfaces.contains(ANNOTATION_TYPE)

    val isInterface: Boolean
        get() = Modifier.isInterface(modifiers)

    val name: String
        get() = type.nonGenericClassName

    private val publicFields: Sequence<JavaField>
        get() = fields.asSequence().filter { it.isPublic }

    val publicInstanceFields: List<JavaField>
        get() = publicFields.filter { !it.isStatic }.toList()

    val getters: List<JavaMethod>
        get() = publicMethods.filter { it.isGetter }.toList()

    val publicMethods: Sequence<JavaMethod>
        get() = methods.asSequence().filter { it.isPublic }

    fun addAnnotation(annotation: JavaAnnotation) {
        _annotations += annotation
    }

    fun addField(field: JavaField) {
        _fields += field
    }

    fun addMethod(method: JavaMethod) {
        _methods += method
    }

    fun addRecordComponent(component: JavaRecordComponent) {
        _recordComponent += component
    }

    /**
     * Returns enum constants defined by this class, if this class is an enum.
     *
     * @return Names of enum constants, in the order they appear
     * @throws IllegalStateException if the class is not an enum
     */
    val enumConstants: List<String>
        get() {
            check(isEnum) { "not an enum: $this" }

            return fields.filter { it.isEnumConstant }.map { it.name }
        }

    val kotlinMetadata: KmClass? by lazy {
        findAnnotation(KOTLIN_METADATA_ANNOTATION)?.let { metadata ->
            val header = Metadata(
                kind = metadata.getAttribute("k"),
                metadataVersion = metadata.getAttribute("mv"),
                data1 = metadata.getAttribute("d1"),
                data2 = metadata.getAttribute("d2"),
                extraString = metadata.getAttribute("xs"),
                packageName = metadata.getAttribute("pn"),
                extraInt = metadata.getAttribute("xi")
            )
            (KotlinClassMetadata.readLenient(header) as? KotlinClassMetadata.Class)?.kmClass
        }
    }

    override fun toString() = type.toString()

    fun getField(name: String): JavaField =
        findField(name) ?: throw RuntimeException("field not found $name")

    fun findField(name: String): JavaField? =
        fields.find { name == it.name }

    fun findMethodWithAnnotation(annotationType: JavaType.Basic) =
        methods.find { it.hasAnnotation(annotationType) }

    companion object {
        private val ANNOTATION_TYPE = JavaType.Basic(Annotation::class.java)
        private val KOTLIN_METADATA_ANNOTATION = JavaType.Basic(Metadata::class.java)
    }
}
