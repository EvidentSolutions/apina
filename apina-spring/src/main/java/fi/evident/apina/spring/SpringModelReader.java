package fi.evident.apina.spring;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.Endpoint;
import fi.evident.apina.model.EndpointGroup;
import fi.evident.apina.model.URITemplate;
import fi.evident.apina.spring.java.model.*;
import fi.evident.apina.spring.java.reader.ClassMetadataCollectionLoader;
import fi.evident.apina.spring.java.reader.Classpath;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Builds {@link ApiDefinition} by reading the classes of a Spring Web MVC application.
 */
public final class SpringModelReader {

    private static final QualifiedName REST_CONTROLLER = new QualifiedName("org.springframework.web.bind.annotation.RestController");
    private static final QualifiedName REQUEST_MAPPING = new QualifiedName("org.springframework.web.bind.annotation.RequestMapping");

    private SpringModelReader() { }

    public static ApiDefinition readApiDefinition(Classpath classpath) throws IOException {
        ClassMetadataCollection metadataCollection = ClassMetadataCollectionLoader.load(classpath);

        ApiDefinition api = new ApiDefinition();

        for (JavaClass controllerMetadata : metadataCollection.findClassesWithAnnotation(REST_CONTROLLER)) {
            api.addEndpointGroups(createEndpointGroupForController(controllerMetadata));
        }

        return api;
    }

    private static EndpointGroup createEndpointGroupForController(JavaClass javaClass) {
        EndpointGroup endpointGroup = new EndpointGroup(javaClass.getName().toString());

        for (JavaMethod method : javaClass.getMethods()) {
            if (method.getVisibility() == JavaVisibility.PUBLIC && !method.isStatic() && method.hasAnnotation(REQUEST_MAPPING)) {
                endpointGroup.addEndpoint(createEndpointForMethod(javaClass, method));
            }
        }

        return endpointGroup;
    }

    private static Endpoint createEndpointForMethod(JavaClass javaClass, JavaMethod method) {
        return new Endpoint(method.getName(), resolveUriTemplate(javaClass, method));
    }

    private static URITemplate resolveUriTemplate(JavaClass javaClass, JavaMethod method) {
        String classUrl = findRequestMappingPath(javaClass);
        String methodUrl = findRequestMappingPath(method);
        String springPathTemplate = classUrl + methodUrl;

        // TODO: property parse spring-templates so that we can create a proper URITemplate

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
