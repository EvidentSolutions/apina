package fi.evident.apina.java.reader

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import java.util.*
import java.util.function.Supplier

/**
 * [SignatureVisitor] that resolves generic types for a class:
 *
 *  * generic variables with their bounds if class defines any
 *  * generic super-class signature
 *  * generic signatures of implemented interfaces
 */
internal class ClassSignatureVisitor private constructor() : SignatureVisitor(Opcodes.ASM5) {

    private val typeSchemaBuilder = TypeSchemaBuilder()

    private var superClassBuilder: Supplier<JavaType>? = null

    private val interfaceBuilders = ArrayList<Supplier<JavaType>>()

    override fun visitFormalTypeParameter(name: String?) {
        typeSchemaBuilder.addTypeParameter(name!!)
    }

    override fun visitClassBound(): SignatureVisitor {
        return visitBound()
    }

    override fun visitInterfaceBound(): SignatureVisitor {
        return visitBound()
    }

    private fun visitBound(): SignatureVisitor {
        val visitor = TypeBuildingSignatureVisitor()
        typeSchemaBuilder.addBoundBuilderForLastTypeParameter(visitor)
        return visitor
    }

    val superClass: JavaType?
        get() = superClassBuilder?.get()

    val interfaces: List<JavaType>
        get() = interfaceBuilders.map { it.get() }

    override fun visitSuperclass(): SignatureVisitor {
        typeSchemaBuilder.finishFormalTypes()
        val visitor = TypeBuildingSignatureVisitor()
        superClassBuilder = visitor
        return visitor
    }

    override fun visitInterface(): SignatureVisitor {
        typeSchemaBuilder.finishFormalTypes()
        val visitor = TypeBuildingSignatureVisitor()
        interfaceBuilders += visitor
        return visitor
    }

    override fun visitEnd() {
        typeSchemaBuilder.finishFormalTypes()
    }

    val schema: TypeSchema
        get() = typeSchemaBuilder.schema

    companion object {

        @JvmStatic
        fun parse(signature: String): ClassSignatureVisitor {
            val visitor = ClassSignatureVisitor()
            SignatureReader(signature).accept(visitor)
            return visitor
        }
    }
}
