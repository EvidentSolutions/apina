package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import java.util.*
import java.lang.reflect.Array as ReflectArray

class JavaAnnotation(val name: JavaType.Basic) {

    /**
     * Attributes of the annotation.
     *
     * Type of values depends on the type of attributes:
     *
     *  * for basic Java types (String, Integer, etc) corresponding Java classes are used
     *  * enumeration values are represented as [EnumValue]s
     *  * nested annotations are represented as nested [JavaAnnotation]s
     *  * class-references are represented as [JavaType.Basic]
     *  * for arrays, everything is stored inside `Object[]`
     */
    private val attributes = LinkedHashMap<String, Any>()

    fun setAttribute(name: String, value: Any) {
        attributes[name] = value
    }

    inline fun <reified T : Any> getAttribute(name: String): T? =
        getAttribute(name, T::class.java)

    inline fun <reified T : Any> getRequiredAttribute(name: String): T =
        getAttribute(name, T::class.java) ?: error("Required attribute $name missing for $name")

    fun <T : Any> getAttribute(name: String, type: Class<T>): T? {
        val value = attributes[name] ?: return null

        return type.cast(when {
            value.javaClass.isArray && type.isArray && !type.componentType.isAssignableFrom(value.javaClass.componentType) -> {
                val length = ReflectArray.getLength(value)
                val array = ReflectArray.newInstance(type.componentType, length)
                System.arraycopy(value, 0, array, 0, length)
                array
            }
            value.javaClass.isArray && !type.isArray -> {
                val length = ReflectArray.getLength(value)
                if (length == 1)
                    ReflectArray.get(value, 0)
                else
                    error("Expected single element for attribute '$name' of ${this.name}, but got $length")
            }
            else -> value
        })
    }

    fun getAttributeValues(name: String): List<Any?> = when (val value = attributes[name]) {
        null -> emptyList()
        is Array<*> -> value.asList()
        else -> listOf(value)
    }

    override fun toString(): String = buildString {
        append('@').append(name)

        if (attributes.isNotEmpty()) {
            append('(')
            if (attributes.size == 1 && "value" in attributes) {
                writeValue(attributes["value"])

            } else {
                val it = attributes.entries.iterator()
                while (it.hasNext()) {
                    val (name, value) = it.next()

                    append(name).append('=')

                    writeValue(value)

                    if (it.hasNext())
                        append(", ")
                }
            }
            append(')')
        }
    }

    private fun StringBuilder.writeValue(value: Any?) {
        if (value is Array<*>) {
            if (value.size == 1)
                writePrimitive(value[0])
            else
                value.joinTo(this, ",", "{", "}")
        } else {
            writePrimitive(value)
        }
    }

    private fun StringBuilder.writePrimitive(value: Any?) {
        if (value is String) {
            append('"').append(value.replace("\"", "\\\"")).append('"')
        } else {
            append(value)
        }
    }
}
