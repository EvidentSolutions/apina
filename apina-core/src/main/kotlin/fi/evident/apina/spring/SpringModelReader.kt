package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.java.reader.JavaModelLoader
import fi.evident.apina.model.*
import fi.evident.apina.model.parameters.EndpointParameter
import fi.evident.apina.model.parameters.EndpointPathVariableParameter
import fi.evident.apina.model.parameters.EndpointRequestBodyParameter
import fi.evident.apina.model.parameters.EndpointRequestParamParameter
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.spring.NameTranslator.translateEndpointGroupName

/**
 * Builds [ApiDefinition] by reading the classes of a Spring Web MVC application.
 */
class SpringModelReader private constructor(private val classes: JavaModel, private val settings: TranslationSettings) {

    private val api = ApiDefinition()
    private val annotationResolver = SpringAnnotationResolver(classes)

    private fun createEndpointsForControllers() {
        for (controllerMetadata in classes.findClassesWithAnnotation(REST_CONTROLLER))
            api.addEndpointGroups(createEndpointGroupForController(controllerMetadata))
    }

    private fun createEndpointGroupForController(javaClass: JavaClass): EndpointGroup {
        val endpointGroup = EndpointGroup(translateEndpointGroupName(javaClass.name), javaClass.name)

        for (m in javaClass.publicMethods)
            if (!m.isStatic && annotationResolver.hasAnnotation(m, REQUEST_MAPPING))
                endpointGroup.addEndpoint(createEndpointForMethod(m))

        return endpointGroup
    }

    private fun createEndpointForMethod(method: JavaMethod): Endpoint {
        val responseBody = resolveResponseBody(method)

        val endpoint = Endpoint(method.name, resolveUriTemplate(method), responseBody)
        resolveRequestMethod(method)?.let { endpoint.method = it }

        val typeTranslator = JacksonTypeTranslator(settings, classes, api)
        val env = method.environment
        for (parameter in method.parameters)
            parseParameter(typeTranslator, parameter, env, method)?.let { endpoint.addParameter(it) }

        return endpoint
    }

    private fun resolveResponseBody(method: JavaMethod): ApiType? {
        val returnType = method.returnType

        if (!returnType.isVoid) {
            val typeTranslator = JacksonTypeTranslator(settings, classes, api)
            return typeTranslator.translateType(returnType, method, method.environment)
        } else {
            return null
        }
    }

    private fun parseParameter(typeTranslator: JacksonTypeTranslator, parameter: JavaParameter, env: TypeEnvironment, method: JavaMethod): EndpointParameter? {
        val name = parameter.name ?: throw EndpointParameterNameNotDefinedException(method)
        val type = typeTranslator.translateType(parameter.type, parameter, env)

        if (annotationResolver.hasAnnotation(parameter, REQUEST_BODY))
            return EndpointRequestBodyParameter(name, type)

        val requestParam = annotationResolver.findAnnotation(parameter, REQUEST_PARAM)
        if (requestParam != null)
            return EndpointRequestParamParameter(name, requestParam.getAttribute("name"), type)

        val pathVariable = annotationResolver.findAnnotation(parameter, PATH_VARIABLE)
        if (pathVariable != null)
            return EndpointPathVariableParameter(name, pathVariable.getAttribute("name"), type)

        return null
    }

    private fun resolveUriTemplate(method: JavaMethod): URITemplate {
        val classUrl = findRequestMappingPath(method.owningClass)
        val methodUrl = findRequestMappingPath(method)

        return parseUriTemplate(classUrl + methodUrl)
    }

    internal fun findRequestMappingPath(element: JavaAnnotatedElement): String {
        val annotation = annotationResolver.findAnnotation(element, REQUEST_MAPPING)
        val value = annotation?.getAttribute<String>("path") ?: ""
        if (value.isEmpty() || value.startsWith("/"))
            return value
        else
            return '/' + value
    }

    private fun resolveRequestMethod(javaMethod: JavaMethod): HTTPMethod? =
            findHttpMethod(javaMethod) ?: findHttpMethod(javaMethod.owningClass)

    private fun findHttpMethod(element: JavaAnnotatedElement): HTTPMethod? =
            annotationResolver.findAnnotation(element, REQUEST_MAPPING)?.getUniqueAttributeValue<EnumValue>("method")
                    ?.let { HTTPMethod.valueOf(it.constant) }

    companion object {

        private val REST_CONTROLLER = JavaType.Basic("org.springframework.web.bind.annotation.RestController")
        private val REQUEST_MAPPING = JavaType.Basic("org.springframework.web.bind.annotation.RequestMapping")
        private val REQUEST_BODY = JavaType.Basic("org.springframework.web.bind.annotation.RequestBody")
        private val REQUEST_PARAM = JavaType.Basic("org.springframework.web.bind.annotation.RequestParam")
        private val PATH_VARIABLE = JavaType.Basic("org.springframework.web.bind.annotation.PathVariable")

        fun readApiDefinition(classpath: Classpath, settings: TranslationSettings): ApiDefinition {
            val reader = SpringModelReader(JavaModelLoader.load(classpath), settings)

            reader.createEndpointsForControllers()

            return reader.api
        }
    }
}
