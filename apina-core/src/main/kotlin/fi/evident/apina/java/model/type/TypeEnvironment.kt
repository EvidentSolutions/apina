package fi.evident.apina.java.model.type

import java.util.*

class TypeEnvironment {

    private val env = HashMap<JavaType.Variable, JavaType>()

    private constructor()

    constructor(parentSchema: TypeSchema, childSchema: TypeSchema) {
        addBindingsFromSchema(parentSchema)
        addBindingsFromSchema(childSchema)
    }

    private fun addBindingsFromSchema(schema: TypeSchema) {
        for (v in schema.variables)
            this[v] = effectiveBounds(schema.getTypeBounds(v))
    }

    operator fun set(v: JavaType.Variable, type: JavaType) {
        if (v == type) {
            // If we try to install a binding to the same variable name, it means that
            // we actually already have a super-class variable of the same name. We can
            // just keep the old binding around because it points to the right ultimate
            // target anyway. Part of the problem is that in binding 'T=T' we have no way
            // to specify that left and right T are actually two separate variables in
            // the program. Therefore we'd end up with a self referring infinite cycle.
            // However, just omitting the binding gives correct result.
            return
        }
        env[v] = type
    }

    fun resolve(arguments: List<JavaType>): List<JavaType> = arguments.map { it.resolve(this) }

    fun lookup(type: JavaType.Variable): JavaType? = env[type]

    override fun toString(): String = env.toString()

    companion object {

        fun empty(): TypeEnvironment = TypeEnvironment()

        private fun effectiveBounds(bounds: List<JavaType>): JavaType {
            // TODO: merge the bounds instead of picking the first one
            return if (!bounds.isEmpty())
                bounds[0]
            else
                JavaType.Basic(Any::class.java)
        }
    }
}
