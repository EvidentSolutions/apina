package fi.evident.apina.output.common

/**
 * Represents raw piece of code written as it is when passed to [CodeWriter.writeValue].
 */
internal class RawCode(private val value: Any) {

    override fun toString() = value.toString()
}
