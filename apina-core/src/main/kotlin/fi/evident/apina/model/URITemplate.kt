package fi.evident.apina.model

/**
 * Represents a template for URLs.
 */
data class URITemplate(private val template: String) {
    override fun toString() = template
}
