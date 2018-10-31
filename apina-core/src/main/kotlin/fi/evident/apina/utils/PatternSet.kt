package fi.evident.apina.utils

import org.intellij.lang.annotations.Language
import java.util.*
import java.util.regex.Pattern

/**
 * A union of regex-patterns.
 */
class PatternSet {

    private val patterns = ArrayList<Pattern>()

    fun addPattern(@Language("RegExp") pattern: String) {
        patterns.add(Pattern.compile(pattern))
    }

    val isEmpty: Boolean
        get() = patterns.isEmpty()

    operator fun contains(s: CharSequence) = patterns.any { it.matcher(s).matches() }
}
