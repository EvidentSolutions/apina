package fi.evident.apina.spring

import fi.evident.apina.model.URITemplate

/**
 * Converts URI-template in Spring format to plain URI-template, removing
 * the specified regex constraints from variables.
 */
fun parseSpringUriTemplate(template: String): URITemplate {
    val parser = SpringUriTemplateParser(template)
    parser.parse()
    return URITemplate(parser.result.toString())
}

private class SpringUriTemplateParser(private val template: String) {

    private var pos = 0
    val result = StringBuilder()

    fun parse() {
        while (hasMore()) {
            readPlainText()

            if (hasMore())
                readVariable()
        }
    }

    private fun readChar(): Char {
        check(hasMore()) { "unexpected end of input" }

        return template[pos++]
    }

    private fun readVariable() {
        check(readChar() == '{') { "expected '{'" }

        var braceLevel = 0
        val start = pos

        while (hasMore()) {
            when (template[pos++]) {
                '\\' -> readChar() // skip next
                '{' -> braceLevel++
                '}' -> if (braceLevel == 0) {
                    val variable = template.substring(start, pos - 1)
                    val colonIndex = variable.indexOf(':')

                    result.append('{').append(if (colonIndex == -1) variable else variable.substring(0, colonIndex)).append('}')
                    return
                } else {
                    braceLevel--
                }
            }
        }

        error("unexpected end of input for template '$template'")
    }

    private fun readPlainText() {
        while (hasMore() && template[pos] != '{')
            result.append(template[pos++])
    }

    private fun hasMore() = pos < template.length
}
