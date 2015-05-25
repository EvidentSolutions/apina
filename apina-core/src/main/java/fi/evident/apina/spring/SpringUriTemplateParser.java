package fi.evident.apina.spring;

import fi.evident.apina.model.URITemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SpringUriTemplateParser {

    private static final Pattern TEMPLATE_VARS_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

    /**
     * Converts URI-template in Spring format to plain URI-template, removing
     * the specified regex constraints from variables.
     */
    static URITemplate parseUriTemplate(String template) {
        StringBuilder result = new StringBuilder();

        Matcher matcher = TEMPLATE_VARS_PATTERN.matcher(template);
        int end = 0;

        while (matcher.find()) {
            result.append(template.substring(end, matcher.start()));
            result.append('{');

            String match = matcher.group(1);
            int colonIndex = match.indexOf(':');
            result.append(colonIndex == -1 ? match : match.substring(0, colonIndex));

            result.append('}');
            end = matcher.end();
        }

        result.append(template.substring(end, template.length()));

        return new URITemplate(result.toString());
    }
}
