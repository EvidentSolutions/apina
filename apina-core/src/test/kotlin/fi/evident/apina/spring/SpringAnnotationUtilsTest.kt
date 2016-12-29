package fi.evident.apina.spring

import fi.evident.apina.java.model.JavaAnnotation
import fi.evident.apina.java.model.type.JavaType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpringAnnotationUtilsTest {

    @Test
    fun undefinedRequestParamName() {
        val annotation = JavaAnnotation(REQUEST_PARAM)
        assertNull(SpringAnnotationUtils.getRequestParamName(annotation))
    }

    @Test
    fun requestParamNameAsValueAttribute() {
        val annotation = JavaAnnotation(REQUEST_PARAM)
        annotation.setAttribute("value", "foo")

        assertEquals("foo", SpringAnnotationUtils.getRequestParamName(annotation))
    }

    @Test
    fun requestParamNameAsNameAttribute() {
        val annotation = JavaAnnotation(REQUEST_PARAM)
        annotation.setAttribute("name", "foo")

        assertEquals("foo", SpringAnnotationUtils.getRequestParamName(annotation))
    }

    @Test
    fun requestMappingPathFromValue() {
        val annotation = JavaAnnotation(REQUEST_MAPPING)
        annotation.setAttribute("value", "/foo")

        assertEquals("/foo", SpringAnnotationUtils.getRequestMappingPath(annotation))
    }

    @Test
    fun requestMappingPathFromPath() {
        val annotation = JavaAnnotation(REQUEST_MAPPING)
        annotation.setAttribute("path", "/foo")

        assertEquals("/foo", SpringAnnotationUtils.getRequestMappingPath(annotation))
    }

    companion object {

        private val REQUEST_PARAM = JavaType.Basic("org.springframework.web.bind.annotation.RequestParam")
        private val REQUEST_MAPPING = JavaType.Basic("org.springframework.web.bind.annotation.RequestMapping")
    }
}
