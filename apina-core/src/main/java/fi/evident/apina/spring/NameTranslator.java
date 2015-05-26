package fi.evident.apina.spring;

import static fi.evident.apina.utils.StringUtils.stripSuffix;
import static java.lang.Math.max;

/**
 * Translates various Java names to api names.
 */
final class NameTranslator {
    public static String translateEndpointGroupName(String name) {
        return stripSuffix(translateClassName(name), "Controller");
    }

    public static String translateClassName(String name) {
        int lastDot = max(name.lastIndexOf('.'), name.lastIndexOf('$'));
        if (lastDot != -1)
            return name.substring(lastDot + 1);
        else
            return name;
    }
}
