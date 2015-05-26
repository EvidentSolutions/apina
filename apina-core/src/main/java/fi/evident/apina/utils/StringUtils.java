package fi.evident.apina.utils;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

public final class StringUtils {

    private StringUtils() { }

    public static String uncapitalize(String s) {
        if (!s.isEmpty() && isUpperCase(s.charAt(0)))
            return toLowerCase(s.charAt(0)) + s.substring(1);
        else
            return s;
    }

    public static String stripSuffix(String s, String suffix) {
        return s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s;
    }
}
