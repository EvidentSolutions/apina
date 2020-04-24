package fi.evident.apina.spring

import fi.evident.apina.java.model.*
import fi.evident.apina.java.model.type.BoundClass
import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeEnvironment
import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.java.reader.ClasspathClassDataLoader
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
import org.slf4j.LoggerFactory

/**
 * Builds [ApiDefinition] by reading the classes of a Spring Web MVC application.
 */
class SpringModelReader private constructor(private val classes: JavaModel, private val settings: TranslationSettings) {

    private val api = ApiDefinition()
    private val annotationResolver = SpringAnnotationResolver(classes)

    private fun createEndpointsForControllers() {
        for (controllerMetadata in classes.findClassesWithAnnotation(settings::isProcessableController, REST_CONTROLLER))
            api.addEndpointGroups(createEndpointGroupForController(controllerMetadata))
    }

    private fun createEndpointGroupForController(javaClass: JavaClass): EndpointGroup {
        val endpointGroup = EndpointGroup(translateEndpointGroupName(javaClass.name))

        val boundClass = BoundClass(javaClass, TypeEnvironment.empty())

        for (cl in classes.classesUpwardsFrom(boundClass))
            for (m in cl.javaClass.publicMethods)
                if (!m.isStatic && annotationResolver.hasAnnotation(m, REQUEST_MAPPING))
                    endpointGroup.addEndpoint(createEndpointForMethod(m, javaClass, cl))

        return endpointGroup
    }

    private fun createEndpointForMethod(method: JavaMethod, owningClass: JavaClass, boundClass: BoundClass): Endpoint {
        val endpoint = Endpoint(
            name = method.name,
            uriTemplate = resolveUriTemplate(method, owningClass),
            responseBody = resolveResponseBody(method, boundClass.environment),
            generateUrlMethod = settings.isUrlEndpoint(owningClass.name, method.name)
        )

        resolveRequestMethod(method)?.let { endpoint.method = it }

        val typeTranslator = JacksonTypeTranslator(settings, classes, api)
        for (parameter in method.parameters)
            parseParameter(typeTranslator, parameter, boundClass.environment, method)?.let { endpoint.addParameter(it) }

        return endpoint
    }

    private fun resolveResponseBody(method: JavaMethod, env: TypeEnvironment): ApiType? {
        val returnType = method.returnType

        return if (!returnType.isVoid) {
            val typeTranslator = JacksonTypeTranslator(settings, classes, api)
            typeTranslator.translateType(unwrapReturnType(returnType), method, env)
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

        // The code here is a bit subtle: we don't wish to translate the type unless it is actually needed
        // because translating the type will add it to the set of used types and it will be written to the model.
        // This means that adding e.g. a HttpServletRequest parameter to controller will end up writing
        // HttpServletRequest to the generated model, which is clearly not something we want. Therefore instead of
        // declaring type as a variable here, we define a function that will be called when it's sure that we need it.
        fun type() = typeTranslator.translateType(parameter.type, parameter, env)

        if (annotationResolver.hasAnnotation(parameter, REQUEST_BODY))
            return EndpointRequestBodyParameter(name, type())

        val requestParam = annotationResolver.findAnnotation(parameter, REQUEST_PARAM)
        if (requestParam != null)
            return EndpointRequestParamParameter(name, requestParam.getAttribute("name"), type())

        val pathVariable = annotationResolver.findAnnotation(parameter, PATH_VARIABLE)
        if (pathVariable != null)
            return EndpointPathVariableParameter(name, pathVariable.getAttribute("name"), type())

        return null
    }

    private fun resolveUriTemplate(method: JavaMethod, owningClass: JavaClass): URITemplate {
        val classUrl = findRequestMappingPath(owningClass)
        val methodUrl = findRequestMappingPath(method)

        val url = settings.normalizeUrl(classUrl + methodUrl)
        return parseSpringUriTemplate(url)
    }

    private fun findRequestMappingPath(element: JavaAnnotatedElement): String {
        val annotation = annotationResolver.findAnnotation(element, REQUEST_MAPPING)
        val value = annotation?.getAttribute("path") ?: ""
        return if (value.isEmpty() || value.startsWith("/"))
            value
        else
            "/$value"
    }

    private fun resolveRequestMethod(javaMethod: JavaMethod): HTTPMethod? =
        findHttpMethod(javaMethod) ?: findHttpMethod(javaMethod.owningClass)

    private fun findHttpMethod(element: JavaAnnotatedElement): HTTPMethod? =
        annotationResolver.findAnnotation(element, REQUEST_MAPPING)?.getFirstAttributeValue<EnumValue>("method")
            ?.let { HTTPMethod.valueOf(it.constant) }

    companion object {

        private val log = LoggerFactory.getLogger(SpringModelReader::class.java)
        val RESPONSE_WRAPPERS = listOf(HTTP_ENTITY, RESPONSE_ENTITY, CALLABLE)

        fun readApiDefinition(model: JavaModel, settings: TranslationSettings): ApiDefinition {
            val reader = SpringModelReader(model, settings)

            reader.createEndpointsForControllers()

            return reader.api
        }

        fun readApiDefinition(classpath: Classpath, settings: TranslationSettings): ApiDefinition {
            ClasspathClassDataLoader(classpath).use { loader ->
                val start = System.currentTimeMillis()
                log.debug("Loaded {} classes in {} ms", loader.classNames.size, System.currentTimeMillis() - start)

                val duplicates = loader.duplicateClassNames
                if (duplicates.isNotEmpty()) {
                    log.warn(
                        "There were {} classes with multiple definitions in classpath. Ignoring duplicate definitions.",
                        duplicates.size
                    )
                    log.debug("Classes with multiple definitions: {}", duplicates)
                }

                return readApiDefinition(JavaModel(loader), settings)
            }
        }
    }
}
