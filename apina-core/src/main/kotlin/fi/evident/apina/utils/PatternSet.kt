package fi.evident.apina.utils

import org.intellij.lang.annotations.Language
import java.util.*
import java.util.regex.Pattern

/**
 * A union of regex-patterns.
 */
class PatternSet {

    private val patterns = ArrayList<Pattern>()

    fun addPattern(pattern: Pattern) {
        patterns.add(pattern)
    }

    fun addPattern(@Language("RegExp") pattern: String) {
        addPattern(Pattern.compile(pattern))
    }

    operator fun contains(s: CharSequence) = patterns.any { it.matcher(s).matches() }
}
