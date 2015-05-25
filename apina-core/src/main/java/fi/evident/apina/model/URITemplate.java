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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URITemplate that = (URITemplate) o;

        return template.equals(that.template);
    }

    @Override
    public int hashCode() {
        return template.hashCode();
    }

    @Override
    public String toString() {
        return template;
    }
}
