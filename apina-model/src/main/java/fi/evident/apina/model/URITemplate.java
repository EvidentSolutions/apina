package fi.evident.apina.model;

import static java.util.Objects.requireNonNull;

/**
 * Represents a template for URLs.
 */
public final class URITemplate {

    private final String template;

    public URITemplate(String template) {
        this.template = requireNonNull(template);
    }

    @Override
    public String toString() {
        return template;
    }
}
