package fi.evident.apina.utils

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternSetTest {

    private val set = PatternSet()

    @Test
    fun emptySetMatchesNothing() {
        assertFalse("" in set)
        assertFalse("foo" in set)
    }

    @Test
    fun setWithSinglePatternMatchesIfPatternMatches() {
        set.addPattern("foo.*")

        assertFalse("" in set)
        assertFalse("bar" in set)
        assertTrue("foo" in set)
        assertTrue("foobar" in set)
    }

    @Test
    fun setWithMultiplePatternsMatchesIfAnyPatternMatches() {
        set.addPattern("foo.*")
        set.addPattern("bar.*")
        set.addPattern("baz.*")

        assertFalse("" in set)

        assertTrue("foo" in set)
        assertTrue("bar" in set)
        assertTrue("baz" in set)
        assertTrue("baz-quux" in set)

        assertFalse("quux" in set)

        val time = Instant.now()
        println(time.epochSecond)
        println(time.nano)
        println(time.toString())

    }
}
