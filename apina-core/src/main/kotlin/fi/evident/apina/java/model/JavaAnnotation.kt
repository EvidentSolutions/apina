package fi.evident.apina.java.model

import fi.evident.apina.java.model.type.JavaType
import java.util.*
import kotlin.collections.asList
import kotlin.collections.joinTo
import java.lang.reflect.Array as ReflectArray

class JavaAnnotation(
        /** Name of the annotation type  */
        val name: JavaType.Basic) {

    /**
     * Attributes of the annotation.

     * Type of values depends on the type of attributes:
     *
     *  * for basic Java types (String, Integer, etc) corresponding Java classes are used
     *  * enumeration values are represented as [EnumValue]s
     *  * nested annotations are represented as nested [JavaAnnotation]s
     *  * for arrays, everything is stored inside `Object[]`
     *
     */
    private val attributes = LinkedHashMap<String, Any>()

    fun setAttribute(name: String, value: Any) {
        attributes.put(name, value)
    }

    fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    fun <T> getAttribute(name: String, type: Class<T>): T? {
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

    fun <T> findUniqueAttributeValue(name: String, type: Class<T>): T? {
        val values = getAttributeValues(name)

        return when (values.size) {
            0 -> null
            1 -> type.cast(values[0])
            else -> throw IllegalArgumentException("multiple values for $name in $this")
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('@').append(name)

        if (attributes.size == 1 && attributes.containsKey("value")) {
            sb.append('(')
            writeValue(sb, attributes["value"])
            sb.append(')')

        } else if (!attributes.isEmpty()) {
            sb.append('(')
            val it = attributes.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                val name = entry.key
                val value = entry.value

                sb.append(name).append('=')

                writeValue(sb, value)

                if (it.hasNext())
                    sb.append(", ")
            }
            sb.append(')')
        }

        return sb.toString()
    }

    private fun writeValue(sb: StringBuilder, value: Any?) {
        if (value is Array<*>) {
            if (value.size == 1)
                writePrimitive(sb, value[0])
            else
                value.joinTo(sb, ",", "{", "}")
        } else {
            writePrimitive(sb, value)
        }
    }

    private fun writePrimitive(sb: StringBuilder, value: Any?) {
        if (value is String) {
            sb.append('"').append(value.replace("\"", "\\\"")).append('"')
        } else {
            sb.append(value)
        }
    }
}
