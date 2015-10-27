package fi.evident.apina.spring;

import fi.evident.apina.java.model.*;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.reader.ClassMetadataCollectionLoader;
import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.*;
import fi.evident.apina.model.parameters.EndpointParameter;
import fi.evident.apina.model.parameters.EndpointPathVariableParameter;
import fi.evident.apina.model.parameters.EndpointRequestBodyParameter;
import fi.evident.apina.model.parameters.EndpointRequestParamParameter;
import fi.evident.apina.model.settings.TranslationSettings;
import fi.evident.apina.model.type.ApiType;

import java.io.IOException;
import java.util.Optional;

import static fi.evident.apina.spring.NameTranslator.translateEndpointGroupName;
import static fi.evident.apina.spring.SpringUriTemplateParser.parseUriTemplate;
import static java.util.Objects.requireNonNull;

/**
 * Builds {@link ApiDefinition} by reading the classes of a Spring Web MVC application.
 */
public final class SpringModelReader {

    private final ClassMetadataCollection classes;
    private final TranslationSettings settings;
    private final ApiDefinition api = new ApiDefinition();

    private static final JavaBasicType REST_CONTROLLER = new JavaBasicType("org.springframework.web.bind.annotation.RestController");
    private static final JavaBasicType REQUEST_MAPPING = new JavaBasicType("org.springframework.web.bind.annotation.RequestMapping");
    private static final JavaBasicType REQUEST_BODY = new JavaBasicType("org.springframework.web.bind.annotation.RequestBody");
    private static final JavaBasicType REQUEST_PARAM = new JavaBasicType("org.springframework.web.bind.annotation.RequestParam");
    private static final JavaBasicType PATH_VARIABLE = new JavaBasicType("org.springframework.web.bind.annotation.PathVariable");

    private SpringModelReader(ClassMetadataCollection classes, TranslationSettings settings) {
        this.classes = requireNonNull(classes);
        this.settings = requireNonNull(settings);
    }

    public static ApiDefinition readApiDefinition(Classpath classpath, TranslationSettings settings) throws IOException {
        SpringModelReader reader = new SpringModelReader(ClassMetadataCollectionLoader.load(classpath), settings);

        reader.createEndpointsForControllers();

        return reader.api;
    }

    private void createEndpointsForControllers() {
        for (JavaClass controllerMetadata : classes.findClassesWithAnnotation(REST_CONTROLLER))
            api.addEndpointGroups(createEndpointGroupForController(controllerMetadata));
    }

    private EndpointGroup createEndpointGroupForController(JavaClass javaClass) {
        EndpointGroup endpointGroup = new EndpointGroup(translateEndpointGroupName(javaClass.getName()), javaClass.getName());

        javaClass.getPublicMethods()
                .filter(m -> !m.isStatic() && m.hasAnnotation(REQUEST_MAPPING))
                .map(this::createEndpointForMethod)
                .forEach(endpointGroup::addEndpoint);

        return endpointGroup;
    }

    private Endpoint createEndpointForMethod(JavaMethod method) {
        Optional<ApiType> responseBody = resolveResponseBody(method);

        Endpoint endpoint = new Endpoint(method.getName(), resolveUriTemplate(method), responseBody);
        resolveRequestMethod(method).ifPresent(endpoint::setMethod);

        JacksonTypeTranslator typeTranslator = new JacksonTypeTranslator(settings, classes, method.getEffectiveSchema(), api);
        for (JavaParameter parameter : method.getParameters())
            parseParameter(typeTranslator, parameter).ifPresent(endpoint::addParameter);

        return endpoint;
    }

    private static Optional<EndpointParameter> parseParameter(JacksonTypeTranslator typeTranslator, JavaParameter parameter) {
        String name = parameter.getName().orElse("?");
        ApiType type = typeTranslator.translateType(parameter.getType());

        if (parameter.hasAnnotation(REQUEST_BODY)) {
            return Optional.of(new EndpointRequestBodyParameter(name, type));

        } else if (parameter.hasAnnotation(REQUEST_PARAM)) {
            Optional<String> requestParam = parameter.getAnnotation(REQUEST_PARAM).getAttribute("value", String.class);
            return Optional.of(new EndpointRequestParamParameter(name, requestParam, type));

        } else if (parameter.hasAnnotation(PATH_VARIABLE)) {
            Optional<String> pathVariable = parameter.getAnnotation(PATH_VARIABLE).getAttribute("value", String.class);
            return Optional.of(new EndpointPathVariableParameter(name, pathVariable, type));

        } else {
            return Optional.empty();
        }
    }

    private Optional<ApiType> resolveResponseBody(JavaMethod method) {
        JavaType returnType = method.getReturnType();

        if (!returnType.isVoid()) {
            JacksonTypeTranslator typeTranslator = new JacksonTypeTranslator(settings, classes, method.getEffectiveSchema(), api);
            return Optional.of(typeTranslator.translateType(returnType));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<HTTPMethod> resolveRequestMethod(JavaMethod javaMethod) {
        Optional<HTTPMethod> method = findHttpMethod(javaMethod);
        if (method.isPresent())
            return method;
        else
            return findHttpMethod(javaMethod.getOwningClass());
    }

    private static Optional<HTTPMethod> findHttpMethod(JavaAnnotatedElement element) {
        return element.findUniqueAnnotationAttributeValue(REQUEST_MAPPING, "method", EnumValue.class)
                .map(v -> HTTPMethod.valueOf(v.getConstant()));
    }

    private static URITemplate resolveUriTemplate(JavaMethod method) {
        String classUrl = findRequestMappingPath(method.getOwningClass());
        String methodUrl = findRequestMappingPath(method);

        return parseUriTemplate(classUrl + methodUrl);
    }

    static String findRequestMappingPath(JavaAnnotatedElement element) {
        String value = element.findUniqueAnnotationAttributeValue(REQUEST_MAPPING, "value", String.class).orElse("");
        if (value.isEmpty() || value.startsWith("/"))
            return value;
        else
            return '/' + value;
    }
}
