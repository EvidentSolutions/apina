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
