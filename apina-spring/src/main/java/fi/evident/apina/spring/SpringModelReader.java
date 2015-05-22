package fi.evident.apina.spring;

import fi.evident.apina.java.model.*;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.reader.ClassMetadataCollectionLoader;
import fi.evident.apina.java.reader.Classpath;
import fi.evident.apina.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
        EndpointGroup endpointGroup = new EndpointGroup(javaClass.getName());

        for (JavaMethod method : javaClass.getMethods()) {
            if (method.getVisibility() == JavaVisibility.PUBLIC && !method.isStatic() && method.hasAnnotation(REQUEST_MAPPING)) {
                endpointGroup.addEndpoint(createEndpointForMethod(javaClass, method));
            }
        }

        return endpointGroup;
    }

    private Endpoint createEndpointForMethod(JavaClass javaClass, JavaMethod method) {
        Optional<TypeName> requestBody = resolveRequestBody(method);
        Optional<TypeName> responseBody = resolveResponseBody(method);

        return new Endpoint(method.getName(), resolveUriTemplate(javaClass, method), requestBody, responseBody);
    }

    private Optional<TypeName> resolveResponseBody(JavaMethod method) {
        JavaType returnType = method.getReturnType();

        if (!returnType.isVoid())
            return Optional.of(resolveDataType(returnType));
        else
            return Optional.empty();
    }

    private Optional<TypeName> resolveRequestBody(JavaMethod method) {
        return method.getParameters().stream()
                .filter(p -> p.hasAnnotation(REQUEST_BODY))
                .map(JavaParameter::getType)
                .map(this::resolveDataType)
                .findAny();
    }

    private TypeName resolveDataType(JavaType javaType) {
        // TODO: resolve the JavaClass based on javaType, build apina-model -compatible representation for it, and register it
        return new TypeName(javaType.toString());
    }

    private static URITemplate resolveUriTemplate(JavaClass javaClass, JavaMethod method) {
        String classUrl = findRequestMappingPath(javaClass);
        String methodUrl = findRequestMappingPath(method);
        String springPathTemplate = classUrl + methodUrl;

        // TODO: parse spring-templates so that we can create a proper URITemplate

        return new URITemplate(springPathTemplate);
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
