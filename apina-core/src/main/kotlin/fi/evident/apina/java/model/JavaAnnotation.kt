package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import java.util.*
import kotlin.collections.asList
import kotlin.collections.joinTo
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
        attributes.put(name, value)
    }

    inline fun <reified T : Any> getAttribute(name: String): T? {
        return getAttribute(name, T::class.java)
    }

    fun <T : Any> getAttribute(name: String, type: Class<T>): T? {
        val value = attributes[name] ?: return null

        if (value.javaClass.isArray && ReflectArray.getLength(value) == 1 && !type.isArray)
            return type.cast(ReflectArray.get(value, 0))

        return type.cast(value)
    }

    fun getAttributeValues(name: String): List<Any?> {
        val value = attributes[name]
        if (value == null)
            return emptyList()
        else if (value is Array<*>)
            return value.asList()
        else
            return listOf(value)
    }

    override fun toString(): String = buildString {
        append('@').append(name)

        if (attributes.size == 1 && attributes.containsKey("value")) {
            append('(')
            writeValue(attributes["value"])
            append(')')

        } else if (!attributes.isEmpty()) {
            append('(')
            val it = attributes.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                val name = entry.key
                val value = entry.value

                append(name).append('=')

                writeValue(value)

                if (it.hasNext())
                    append(", ")
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
