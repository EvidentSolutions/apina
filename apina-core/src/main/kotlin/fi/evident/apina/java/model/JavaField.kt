package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import org.objectweb.asm.Opcodes
import java.lang.reflect.Modifier
import java.util.*

class JavaField(val name: String,
                val visibility: JavaVisibility,
                val type: JavaType,
                private val modifiers: Int) : JavaAnnotatedElement {

    private val _annotations = ArrayList<JavaAnnotation>()

    override val annotations: List<JavaAnnotation>
        get() = _annotations

    val isEnumConstant: Boolean
        get() = modifiers and Opcodes.ACC_ENUM != 0

    val isPublic: Boolean
        get() = visibility === JavaVisibility.PUBLIC

    val isStatic: Boolean
        get() = Modifier.isStatic(modifiers)

    fun addAnnotation(annotation: JavaAnnotation) {
        _annotations += annotation
    }

    override fun toString(): String {
        val sb = StringBuilder()

        if (visibility !== JavaVisibility.PACKAGE)
            sb.append(visibility).append(' ')

        if (isStatic)
            sb.append("static ")

        sb.append(type).append(' ').append(name)

        return sb.toString()
    }
}
