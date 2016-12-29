package fi.evident.apina.spring

import fi.evident.apina.model.URITemplate

/**
 * Converts URI-template in Spring format to plain URI-template, removing
 * the specified regex constraints from variables.
 */
fun parseUriTemplate(template: String): URITemplate {
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
        if (!hasMore()) throw IllegalStateException("unexpected end of input")

        return template[pos++]
    }

    private fun readVariable() {
        if (readChar() != '{') throw IllegalStateException("expected '{'")

        var braceLevel = 0
        val start = pos

        while (hasMore()) {
            when (template[pos++]) {
                '\\' -> readChar() // skip next
                '{' -> braceLevel++
                '}' -> if (braceLevel == 0) {
                    val `var` = template.substring(start, pos - 1)
                    val colonIndex = `var`.indexOf(':')

                    result.append('{').append(if (colonIndex == -1) `var` else `var`.substring(0, colonIndex)).append('}')
                    return
                } else {
                    braceLevel--
                }
            }
        }

        throw IllegalStateException("unexpected end of input for template '$template'")
    }

    private fun readPlainText() {
        while (hasMore() && template[pos] != '{')
            result.append(template[pos++])
    }

    private fun hasMore() = pos < template.length
}
