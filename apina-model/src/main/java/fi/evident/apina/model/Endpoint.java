package fi.evident.apina.model;

import static java.util.Objects.requireNonNull;

/**
 * API endpoint reachable at an URL using given method and parameters. An example
 * of an endpoint is a single method on a controller.
 * Represents an single endpoint (e.g. a method in a controller)
 */
public final class Endpoint {

    /** Name of the original source element that specifies this endpoint */
    private final String name;

    /** URI template for the endpoint */
    private final URITemplate uriTemplate;

    /** HTTP method for accessing the endpoint */
    private HTTPMethod method = HTTPMethod.GET;

    public Endpoint(String name, URITemplate uriTemplate) {
        this.name = requireNonNull(name);
        this.uriTemplate = requireNonNull(uriTemplate);
    }

    public URITemplate getUriTemplate() {
        return uriTemplate;
    }

    public String getName() {
        return name;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public void setMethod(HTTPMethod method) {
        this.method = requireNonNull(method);
    }

    @Override
    public String toString() {
        return name + ": " + uriTemplate;
    }
}
