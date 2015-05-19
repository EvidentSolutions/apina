package fi.evident.apina.model;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

/**
 * Represents the whole API of the program: i.e. all {@link EndpointGroup}s.
 */
public final class ApiDefinition {

    private final Collection<EndpointGroup> endpointGroups = new ArrayList<>();

    public Collection<EndpointGroup> getEndpointGroups() {
        return unmodifiableCollection(endpointGroups);
    }

    public void addEndpointGroups(EndpointGroup group) {
        endpointGroups.add(requireNonNull(group));
    }
}
