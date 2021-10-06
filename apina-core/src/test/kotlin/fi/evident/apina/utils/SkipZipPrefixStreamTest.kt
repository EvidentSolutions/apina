package fi.evident.apina.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SkipZipPrefixStreamTest {

    @Test
    fun `everything before PK-prefix is skipped`() {
        val s = SkipZipPrefixStream("skipped_prefix_PK_actual_content".toByteArray().inputStream())
        assertEquals("PK_actual_content", s.reader().use { it.readText() })
    }

    @Test
    fun `stream without PK-header is considered empty`() {
        val s = SkipZipPrefixStream("some_data_and_P_and_more_data".toByteArray().inputStream())
        assertEquals("", s.reader().use { it.readText() })
    }
}
