package fi.evident.apina.spring;

import fi.evident.apina.model.ApiDefinition;
import fi.evident.apina.model.EndpointGroup;
import fi.evident.apina.spring.java.model.*;
import fi.evident.apina.spring.java.reader.ClassMetadataCollectionLoader;
import fi.evident.apina.spring.java.reader.Classpath;

import java.io.IOException;

/**
 * Builds {@link ApiDefinition} by reading the classes of a Spring Web MVC application.
 */
public final class SpringModelReader {

    private static final QualifiedName REST_CONTROLLER = new QualifiedName("org.springframework.web.bind.annotation.RestController");

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
        javaClass.getAnnotations().forEach(System.out::println);
        System.out.println("class " + javaClass.getName() + " {");
        System.out.println();

        for (JavaField javaField : javaClass.getFields()) {
            for (JavaAnnotation annotation : javaField.getAnnotations())
                System.out.println("    " + annotation);
            System.out.println("    " + javaField);
            System.out.println();
        }

        for (JavaMethod javaMethod : javaClass.getMethods()) {
            for (JavaAnnotation annotation : javaMethod.getAnnotations())
                System.out.println("    " + annotation);
            System.out.println("    " + javaMethod);
            System.out.println();
        }

        System.out.println("}");
        System.out.println();

        return new EndpointGroup(javaClass.getName().toString());
    }
}
