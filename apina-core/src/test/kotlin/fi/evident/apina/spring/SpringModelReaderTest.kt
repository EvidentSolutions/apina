package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.spring.SpringModelReader.Companion.findRequestMappingPath
import org.junit.Test
import kotlin.test.assertEquals

class SpringModelReaderTest {

    @Test
    fun requestMappingPath() {
        assertEquals("", findRequestMappingPath(elementWithPathRequestMapping("")))
        assertEquals("/", findRequestMappingPath(elementWithPathRequestMapping("/")))
        assertEquals("/foo", findRequestMappingPath(elementWithPathRequestMapping("/foo")))
        assertEquals("/foo/bar", findRequestMappingPath(elementWithPathRequestMapping("/foo/bar")))
    }

    @Test
    fun emptyRequestMappingPathWithoutLeadingSlashGetsSlashAddedAutomatically() {
        assertEquals("/foo", findRequestMappingPath(elementWithPathRequestMapping("foo")))
        assertEquals("/foo/bar", findRequestMappingPath(elementWithPathRequestMapping("foo/bar")))
    }

    private val REQUEST_MAPPING = JavaType.Basic("org.springframework.web.bind.annotation.RequestMapping")

    private fun elementWithPathRequestMapping(path: String): MockAnnotatedElement {
        val requestMapping = JavaAnnotation(REQUEST_MAPPING)
        requestMapping.setAttribute("value", path)
        return MockAnnotatedElement(requestMapping)
    }
}
