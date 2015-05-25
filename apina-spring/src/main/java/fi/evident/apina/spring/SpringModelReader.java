package fi.evident.apina.spring;

import fi.evident.apina.java.model.*;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.reader.ClassMetadataCollectionLoader;
import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.Endpoint;
import fi.evident.apina.model.EndpointGroup;
import fi.evident.apina.model.URITemplate;
import fi.evident.apina.model.parameters.EndpointParameter;
import fi.evident.apina.model.parameters.EndpointPathVariableParameter;
import fi.evident.apina.model.parameters.EndpointRequestBodyParameter;
import fi.evident.apina.model.parameters.EndpointRequestParamParameter;
import fi.evident.apina.model.type.ApiType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static fi.evident.apina.spring.SpringUriTemplateParser.parseUriTemplate;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * Builds {@link ApiDefinition} by reading the classes of a Spring Web MVC application.
 */
public final class SpringModelReader {

    private final ClassMetadataCollection classes;
    private final ApiDefinition api = new ApiDefinition();

    private static final JavaBasicType REST_CONTROLLER = new JavaBasicType("org.springframework.web.bind.annotation.RestController");
    private static final JavaBasicType REQUEST_MAPPING = new JavaBasicType("org.springframework.web.bind.annotation.RequestMapping");
    private static final JavaBasicType REQUEST_BODY = new JavaBasicType("org.springframework.web.bind.annotation.RequestBody");
    private static final JavaBasicType REQUEST_PARAM = new JavaBasicType("org.springframework.web.bind.annotation.RequestParam");
    private static final JavaBasicType PATH_VARIABLE = new JavaBasicType("org.springframework.web.bind.annotation.PathVariable");

    private SpringModelReader(ClassMetadataCollection classes) {
        this.classes = requireNonNull(classes);
    }

    public static ApiDefinition readApiDefinition(Classpath classpath) throws IOException {
        SpringModelReader reader = new SpringModelReader(ClassMetadataCollectionLoader.load(classpath));

        reader.createEndpointsForControllers();

        return reader.api;
    }

    private void createEndpointsForControllers() {
        for (JavaClass controllerMetadata : classes.findClassesWithAnnotation(REST_CONTROLLER))
            api.addEndpointGroups(createEndpointGroupForController(controllerMetadata));
    }

    private EndpointGroup createEndpointGroupForController(JavaClass javaClass) {
        EndpointGroup endpointGroup = new EndpointGroup(TypeTranslator.translateName(javaClass.getName()), javaClass.getName());

        for (JavaMethod method : javaClass.getMethods()) {
            if (method.getVisibility() == JavaVisibility.PUBLIC && !method.isStatic() && method.hasAnnotation(REQUEST_MAPPING)) {
                endpointGroup.addEndpoint(createEndpointForMethod(method));
            }
        }

        return endpointGroup;
    }

    private Endpoint createEndpointForMethod(JavaMethod method) {
        Optional<ApiType> responseBody = resolveResponseBody(method);

        Endpoint endpoint = new Endpoint(method.getName(), resolveUriTemplate(method), responseBody);

        TypeTranslator typeTranslator = new TypeTranslator(classes, method.getEffectiveSchema());
        for (JavaParameter parameter : method.getParameters())
            parseParameter(typeTranslator, parameter).ifPresent(endpoint::addParameter);

        return endpoint;
    }

    private static Optional<EndpointParameter> parseParameter(TypeTranslator typeTranslator, JavaParameter parameter) {
        String name = parameter.getName().orElse("?");
        ApiType type = typeTranslator.resolveDataType(parameter.getType());

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
            TypeTranslator typeTranslator = new TypeTranslator(classes, method.getEffectiveSchema());
            return Optional.of(typeTranslator.resolveDataType(returnType));
        } else {
            return Optional.empty();
        }
    }

    private static URITemplate resolveUriTemplate(JavaMethod method) {
        String classUrl = findRequestMappingPath(method.getOwningClass());
        String methodUrl = findRequestMappingPath(method);

        return parseUriTemplate(classUrl + methodUrl);
    }

    private static String findRequestMappingPath(JavaAnnotatedElement element) {
        List<Object> values = element.findAnnotation(REQUEST_MAPPING).map(a -> a.getAttributeValues("value")).orElse(emptyList());

        if (values.isEmpty())
            return "";
        else if (values.size() == 1)
            return (String) values.get(0);
        else
            throw new RuntimeException("@RequestMapping -annotations with multiple values are not supported");
    }
}
