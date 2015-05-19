package fi.evident.apina.spring;

import fi.evident.apina.model.ApiDefinition;

import java.nio.file.Path;

/**
 * Builds {@link ApiDefinition} by reading the classes of a Spring Web MVC application.
 */
public final class SpringModelReader {

    private SpringModelReader() { }

    public static ApiDefinition readApiDefinition(Path input) {
        throw new UnsupportedOperationException();
    }
}
