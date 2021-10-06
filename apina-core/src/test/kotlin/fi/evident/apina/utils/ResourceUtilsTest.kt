package fi.evident.apina.utils

import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResourceUtilsTest {

    @Test
    fun `reading resource as string`() {
        assertEquals("Hello, world!", readResourceAsString("test-data/hello-world.txt").trim())
    }

    @Test
    fun `unknown resource`() {
        assertFailsWith<FileNotFoundException> {
            readResourceAsString("unknown-file")
        }
    }
}
