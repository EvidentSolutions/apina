package fi.evident.apina.java.reader

import fi.evident.apina.java.model.type.JavaType
import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor
import java.util.*
import java.util.function.Supplier

/**
 * [SignatureVisitor] implementation that visits the type hierarchy to build generic types.
 */
internal class TypeBuildingSignatureVisitor : SignatureVisitor(Opcodes.ASM5), Supplier<JavaType> {

    private var builder: ((List<JavaType>) -> JavaType)? = null

    private val arguments = ArrayList<() -> JavaType>()

    private fun initBuilder(builder: (List<JavaType>) -> JavaType) {
        if (this.builder != null)
            throw IllegalStateException("tried to initialize builder twice")

        this.builder = builder
    }

    override fun get(): JavaType {
        if (builder == null)
            throw IllegalStateException("no builder defined for visitor")

        val argumentTypes = arguments.map { it() }

        return builder!!(argumentTypes)
    }

    override fun visitBaseType(descriptor: Char) {
        val baseType = parseTypeDescriptor(descriptor.toString())
        initBuilder { args ->
            assert(args.isEmpty())
            baseType
        }
    }

    override fun visitTypeVariable(name: String) {
        initBuilder { args ->
            assert(args.isEmpty())
            JavaType.Variable(name)
        }
    }

    override fun visitArrayType(): SignatureVisitor {
        val nestedVisitor = TypeBuildingSignatureVisitor()

        initBuilder { args ->
            assert(args.isEmpty())
            JavaType.Array(nestedVisitor.get())
        }

        return nestedVisitor
    }

    override fun visitClassType(name: String?) {
        val baseType = parseObjectType(name!!)
        initBuilder { args -> if (args.isEmpty()) baseType else JavaType.Parameterized(baseType, args) }
    }

    override fun visitInnerClassType(name: String) {
        val originalBuilder = builder ?: throw IllegalStateException("no builder")

        builder = { types -> JavaType.InnerClass(originalBuilder(types), name) }
    }

    override fun visitTypeArgument() {
        arguments.add { JavaType.Wildcard.unbounded() }
    }

    override fun visitTypeArgument(wildcard: Char): SignatureVisitor {
        val nestedVisitor = TypeBuildingSignatureVisitor()
        arguments.add(typeBuilderForWildcard(wildcard, { nestedVisitor.get() }))
        return nestedVisitor
    }

    private fun typeBuilderForWildcard(wildcard: Char, typeBuilder: () -> JavaType): () -> JavaType {
        when (wildcard) {
            '=' -> return typeBuilder
            '+' -> return { JavaType.Wildcard.extending(typeBuilder()) }
            '-' -> return { JavaType.Wildcard.withSuper(typeBuilder()) }
            else -> throw IllegalArgumentException("unknown wildcard: " + wildcard)
        }
    }
}
