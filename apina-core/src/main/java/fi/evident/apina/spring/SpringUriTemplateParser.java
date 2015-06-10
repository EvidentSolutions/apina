package fi.evident.apina.spring;

import fi.evident.apina.model.URITemplate;

import static java.util.Objects.requireNonNull;

final class SpringUriTemplateParser {

    private final String template;
    private int pos = 0;
    private final StringBuilder result = new StringBuilder();

    private SpringUriTemplateParser(String template) {
        this.template = requireNonNull(template);
    }

    /**
     * Converts URI-template in Spring format to plain URI-template, removing
     * the specified regex constraints from variables.
     */
    static URITemplate parseUriTemplate(String template) {
        SpringUriTemplateParser parser = new SpringUriTemplateParser(template);
        parser.parse();
        return new URITemplate(parser.result.toString());
    }

    private void parse() {
        while (hasMore()) {
            readPlainText();

            if (hasMore())
                readVariable();
        }
    }

    private char readChar() {
        if (!hasMore()) throw new IllegalStateException("unexpected end of input");

        return template.charAt(pos++);
    }

    private void readVariable() {
        if (readChar() != '{') throw new IllegalStateException("expected '{'");

        int braceLevel = 0;
        int start = pos;

        while (hasMore()) {
            switch (template.charAt(pos++)) {
                case '\\':
                    readChar(); // skip next
                    break;
                case '{':
                    braceLevel++;
                    break;
                case '}':
                    if (braceLevel == 0) {
                        String var = template.substring(start, pos - 1);
                        int colonIndex = var.indexOf(':');

                        result.append('{').append((colonIndex == -1) ? var : var.substring(0, colonIndex)).append('}');
                        return;
                    } else {
                        braceLevel--;
                    }
                    break;
            }
        }

        throw new IllegalStateException("unexpected end of input for template '" + template + '\'');
    }

    private void readPlainText() {
        while (hasMore() && template.charAt(pos) != '{')
            result.append(template.charAt(pos++));
    }

    private boolean hasMore() {
        return pos < template.length();
    }
}
