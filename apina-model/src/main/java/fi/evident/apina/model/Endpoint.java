package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiType;

import java.util.Optional;

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

    private final Optional<ApiType> requestBody;

    private final Optional<ApiType> responseBody;

    /** HTTP method for accessing the endpoint */
    private HTTPMethod method = HTTPMethod.GET;

    public Endpoint(String name, URITemplate uriTemplate, Optional<ApiType> requestBody, Optional<ApiType> responseBody) {
        this.name = requireNonNull(name);
        this.uriTemplate = requireNonNull(uriTemplate);
        this.requestBody = requireNonNull(requestBody);
        this.responseBody = requireNonNull(responseBody);
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
        return name + ": " + uriTemplate + " - " + requestBody.map(ApiType::toString).orElse("{}") + " -> " + responseBody.map(ApiType::toString).orElse("{}");
    }
}
