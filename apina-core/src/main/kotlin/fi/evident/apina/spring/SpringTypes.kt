package fi.evident.apina.spring

import fi.evident.apina.java.model.type.JavaType
import java.util.concurrent.Callable

/**
 * Types that have special meaning to Spring. Not all defined by Spring.
 */
object SpringTypes {
    val ALIAS_FOR = JavaType.Basic("org.springframework.core.annotation.AliasFor")
    val REST_CONTROLLER = JavaType.Basic("org.springframework.web.bind.annotation.RestController")
    val REQUEST_MAPPING = JavaType.Basic("org.springframework.web.bind.annotation.RequestMapping")
    val REQUEST_BODY = JavaType.Basic("org.springframework.web.bind.annotation.RequestBody")
    val REQUEST_PARAM = JavaType.Basic("org.springframework.web.bind.annotation.RequestParam")
    val PATH_VARIABLE = JavaType.Basic("org.springframework.web.bind.annotation.PathVariable")
    val RESPONSE_ENTITY = JavaType.Basic("org.springframework.http.ResponseEntity")
    val HTTP_ENTITY = JavaType.Basic("org.springframework.http.HttpEntity")
    val CALLABLE = JavaType.basic<Callable<*>>()
}
