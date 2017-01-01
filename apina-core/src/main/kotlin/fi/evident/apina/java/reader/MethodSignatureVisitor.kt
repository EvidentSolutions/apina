package fi.evident.apina.java.reader

import fi.evident.apina.java.model.MethodSignature
import fi.evident.apina.java.model.type.JavaType
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor
import java.util.*
import java.util.function.Supplier

/**
 * [SignatureVisitor] that builds method signature for generic methods.
 */
internal class MethodSignatureVisitor : SignatureVisitor(Opcodes.ASM5), Supplier<MethodSignature> {

    private var returnTypeBuilder: Supplier<JavaType>? = null

    private val parameterTypeBuilders = ArrayList<Supplier<JavaType>>()

    private val typeSchemaBuilder = TypeSchemaBuilder()

    override fun visitFormalTypeParameter(name: String) {
        typeSchemaBuilder.addTypeParameter(name)
    }

    override fun visitClassBound() = visitBound()

    override fun visitInterfaceBound() = visitBound()

    private fun visitBound(): SignatureVisitor {
        val visitor = TypeBuildingSignatureVisitor()
        typeSchemaBuilder.addBoundBuilderForLastTypeParameter(visitor)
        return visitor
    }

    override fun visitParameterType(): SignatureVisitor {
        typeSchemaBuilder.finishFormalTypes()
        val visitor = TypeBuildingSignatureVisitor()
        parameterTypeBuilders.add(visitor)
        return visitor
    }

    override fun visitReturnType(): SignatureVisitor {
        typeSchemaBuilder.finishFormalTypes()
        val visitor = TypeBuildingSignatureVisitor()
        returnTypeBuilder = visitor
        return visitor
    }

    override fun get(): MethodSignature {
        if (returnTypeBuilder == null)
            throw IllegalStateException("returnTypeBuilder not initialized")

        return MethodSignature(
                returnTypeBuilder!!.get(),
                parameterTypeBuilders.map { it.get() },
                typeSchemaBuilder.schema)
    }

}
