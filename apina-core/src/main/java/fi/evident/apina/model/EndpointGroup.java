package fi.evident.apina.model;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

/**
 * A group of related {@link Endpoint}s. As an example, web controllers are a group
 * of individual endpoints (methods).
 */
public final class EndpointGroup {

    private final String name;

    /** Name of the original source element that specifies this endpoint group */
    private final String originalName;

    private final Collection<Endpoint> endpoints = new ArrayList<>();

    public EndpointGroup(String name, String originalName) {
        this.name = requireNonNull(name);
        this.originalName = requireNonNull(originalName);
    }

    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(requireNonNull(endpoint));
    }

    public Collection<Endpoint> getEndpoints() {
        return unmodifiableCollection(endpoints);
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getEndpointCount() {
        return endpoints.size();
    }
}
