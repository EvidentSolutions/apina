package fi.evident.apina.utils

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternSetTest {

    private val set = PatternSet()

    @Test
    fun emptySetMatchesNothing() {
        assertFalse(set.test(""))
        assertFalse(set.test("foo"))
    }

    @Test
    fun setWithSinglePatternMatchesIfPatternMatches() {
        set.addPattern("foo.*")

        assertFalse(set.test(""))
        assertFalse(set.test("bar"))
        assertTrue(set.test("foo"))
        assertTrue(set.test("foobar"))
    }

    @Test
    fun setWithMultiplePatternsMatchesIfAnyPatternMatches() {
        set.addPattern("foo.*")
        set.addPattern("bar.*")
        set.addPattern("baz.*")

        assertFalse(set.test(""))

        assertTrue(set.test("foo"))
        assertTrue(set.test("bar"))
        assertTrue(set.test("baz"))
        assertTrue(set.test("baz-quux"))

        assertFalse(set.test("quux"))
    }
}
