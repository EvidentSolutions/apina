package fi.evident.apina.java.reader

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import java.util.*
import java.util.function.Supplier

/**
 * Support for building a collection of type variables.
 *
 * Since self bounds require variable to be created before the bound is
 * translated, the type variables are now built immediately when they are
 * encountered in bytecode and bounds are then added to the existing
 * variable.
 */
internal class TypeSchemaBuilder {

    private var lastVariable: JavaType.Variable? = null

    private var currentBounds: MutableList<Supplier<JavaType>>? = null

    val schema = TypeSchema()

    fun addTypeParameter(name: String) {
        finishFormalTypes()

        val v = JavaType.Variable(name)
        lastVariable = v
        schema.add(v)
        currentBounds = ArrayList<Supplier<JavaType>>()
    }

    fun addBoundBuilderForLastTypeParameter(boundBuilder: Supplier<JavaType>) {
        if (currentBounds == null) throw IllegalStateException("no current bounds")

        currentBounds!!.add(boundBuilder)
    }

    fun finishFormalTypes() {
        if (currentBounds != null) {
            assert(lastVariable != null)

            for (bound in currentBounds!!)
                schema.addBound(lastVariable!!, bound.get())

            currentBounds = null
        }
    }
}
