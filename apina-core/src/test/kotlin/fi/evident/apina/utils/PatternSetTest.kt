package fi.evident.apina.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternSetTest {

    private val set = PatternSet()

    @Test
    fun `empty set matches nothing`() {
        assertFalse("" in set)
        assertFalse("foo" in set)
    }

    @Test
    fun `set with single pattern matches if pattern matches`() {
        set.addPattern("foo.*")

        assertFalse("" in set)
        assertFalse("bar" in set)
        assertTrue("foo" in set)
        assertTrue("foobar" in set)
    }

    @Test
    fun `set with multiple patterns matches if any pattern matches`() {
        set.addPattern("foo.*")
        set.addPattern("bar.*")
        set.addPattern("baz.*")

        assertFalse("" in set)

        assertTrue("foo" in set)
        assertTrue("bar" in set)
        assertTrue("baz" in set)
        assertTrue("baz-quux" in set)

        assertFalse("quux" in set)
    }
}
