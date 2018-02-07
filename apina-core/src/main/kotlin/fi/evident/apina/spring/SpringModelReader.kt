package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.java.reader.loadModel
import fi.evident.apina.model.*
import fi.evident.apina.model.parameters.EndpointParameter
import fi.evident.apina.model.parameters.EndpointPathVariableParameter
import fi.evident.apina.model.parameters.EndpointRequestBodyParameter
import fi.evident.apina.model.parameters.EndpointRequestParamParameter
import fi.evident.apina.model.settings.TranslationSettings
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.spring.SpringTypes.CALLABLE
import fi.evident.apina.spring.SpringTypes.HTTP_ENTITY
import fi.evident.apina.spring.SpringTypes.PATH_VARIABLE
import fi.evident.apina.spring.SpringTypes.REQUEST_BODY
import fi.evident.apina.spring.SpringTypes.REQUEST_MAPPING
import fi.evident.apina.spring.SpringTypes.REQUEST_PARAM
import fi.evident.apina.spring.SpringTypes.RESPONSE_ENTITY
import fi.evident.apina.spring.SpringTypes.REST_CONTROLLER

/**
 * Builds [ApiDefinition] by reading the classes of a Spring Web MVC application.
 */
class SpringModelReader private constructor(private val classes: JavaModel, private val settings: TranslationSettings) {

    private val api = ApiDefinition()
    private val annotationResolver = SpringAnnotationResolver(classes)

    private fun createEndpointsForControllers() {
        for (controllerMetadata in classes.findClassesWithAnnotation(REST_CONTROLLER))
            if (settings.isProcessableController(controllerMetadata.name))
                api.addEndpointGroups(createEndpointGroupForController(controllerMetadata))
    }

    private fun createEndpointGroupForController(javaClass: JavaClass): EndpointGroup {
        val endpointGroup = EndpointGroup(translateEndpointGroupName(javaClass.name))

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

        return if (!returnType.isVoid) {
            val typeTranslator = JacksonTypeTranslator(settings, classes, api)
            typeTranslator.translateType(unwrapReturnType(returnType), method, method.environment)
        } else {
            null
        }
    }

    /**
     * The logical return type can be wrapped in a wrapper type. In this case we want to
     * peek into the wrapper type to extract the logical type.
     */
    private fun unwrapReturnType(type: JavaType): JavaType {
        if (type is JavaType.Parameterized && type.baseType in RESPONSE_WRAPPERS)
            return type.arguments.single()

        return type
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

    private fun findRequestMappingPath(element: JavaAnnotatedElement): String {
        val annotation = annotationResolver.findAnnotation(element, REQUEST_MAPPING)
        val value = annotation?.getAttribute("path") ?: ""
        return if (value.isEmpty() || value.startsWith("/"))
            value
        else
            '/' + value
    }

    private fun resolveRequestMethod(javaMethod: JavaMethod): HTTPMethod? =
            findHttpMethod(javaMethod) ?: findHttpMethod(javaMethod.owningClass)

    private fun findHttpMethod(element: JavaAnnotatedElement): HTTPMethod? =
            annotationResolver.findAnnotation(element, REQUEST_MAPPING)?.getUniqueAttributeValue<EnumValue>("method")
                    ?.let { HTTPMethod.valueOf(it.constant) }

    companion object {

        val RESPONSE_WRAPPERS = listOf(HTTP_ENTITY, RESPONSE_ENTITY, CALLABLE)

        fun readApiDefinition(model: JavaModel, settings: TranslationSettings): ApiDefinition {
            val reader = SpringModelReader(model, settings)

            reader.createEndpointsForControllers()

            return reader.api
        }

        fun readApiDefinition(classpath: Classpath, settings: TranslationSettings): ApiDefinition =
                readApiDefinition(classpath.loadModel(), settings)
    }
}
