package fi.evident.apina.java.model.type

class TypeEnvironment(
    bindings: List<Pair<JavaType.Variable, JavaType>>
) {

    /**
     * The number of generic arguments is typically so slow that an association list should be
     * better fit than hash-map. Furthermore, we retain indices which is important when resolving
     * the types by index.
     */
    private val bindings = bindings.filter { (k, v) ->
        // If we try to install a binding to the same variable name, it means that we actually already have
        // a super-class variable of the same name. We can just keep the old binding around because it points
        // to the right ultimate target anyway. Part of the problem is that in binding 'T=T' we have no way
        // to specify that left and right T are actually two separate variables in the program. Therefore, we'd
        // end up with a self referring infinite cycle.
        k != v
    }

    fun lookup(type: JavaType.Variable): JavaType? = bindings.firstOrNull { it.first == type }?.second
    fun lookup(index: Int): JavaType? = bindings.firstOrNull { it.first == type }?.second

    override fun toString(): String =
        bindings.joinToString(separator = ", ", prefix = "{", postfix = "}") { "${it.first}=${it.second}" }

    companion object {

        fun empty(): TypeEnvironment = TypeEnvironment(emptyList())

        operator fun invoke(parentSchema: TypeSchema, childSchema: TypeSchema) =
            TypeEnvironment(buildList {
                for (schema in listOf(parentSchema, childSchema))
                    for (v in schema.variables)
                        add(v to schema.getTypeBounds(v).toEffectiveBounds())
            })

        private fun List<JavaType>.toEffectiveBounds(): JavaType =
            // TODO: merge the bounds instead of picking the first one
            firstOrNull() ?: JavaType.Basic(Any::class.java)
    }
}
