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

    private lateinit var returnTypeBuilder: Supplier<JavaType>

    private val parameterTypeBuilders = ArrayList<Supplier<JavaType>>()

    private val typeSchemaBuilder = TypeSchemaBuilder()

    override fun visitFormalTypeParameter(name: String) {
        typeSchemaBuilder.addTypeParameter(name)
    }

    override fun visitClassBound() = visitBound()

    override fun visitInterfaceBound() = visitBound()

    private fun visitBound(): SignatureVisitor {
        return TypeBuildingSignatureVisitor().also {
            typeSchemaBuilder.addBoundBuilderForLastTypeParameter(it)
        }
    }

    override fun visitParameterType(): SignatureVisitor {
        typeSchemaBuilder.finishFormalTypes()
        return TypeBuildingSignatureVisitor().also {
            parameterTypeBuilders += it
        }
    }

    override fun visitReturnType(): SignatureVisitor {
        typeSchemaBuilder.finishFormalTypes()
        return TypeBuildingSignatureVisitor().also {
            returnTypeBuilder = it
        }
    }

    override fun get(): MethodSignature = MethodSignature(
        returnTypeBuilder.get(),
        parameterTypeBuilders.map { it.get() },
        typeSchemaBuilder.schema
    )
}
