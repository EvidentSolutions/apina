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

    /** Name of the original source element that specifies this endpoint group */
    private final String name;

    private final Collection<Endpoint> endpoints = new ArrayList<>();

    public EndpointGroup(String name) {
        this.name = requireNonNull(name);
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

    @Override
    public String toString() {
        return name;
    }
}
