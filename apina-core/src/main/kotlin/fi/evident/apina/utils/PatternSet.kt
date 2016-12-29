package fi.evident.apina.utils

import org.intellij.lang.annotations.Language
import java.util.*
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * A union of regex-patterns.
 */
class PatternSet : Predicate<CharSequence> {

    private val patterns = ArrayList<Pattern>()

    fun addPattern(pattern: Pattern) {
        patterns.add(pattern)
    }

    fun addPattern(@Language("RegExp") pattern: String) {
        addPattern(Pattern.compile(pattern))
    }

    override fun test(s: CharSequence) = patterns.any { it.matcher(s).matches() }
}
