package fi.evident.apina.utils;

public final class StringUtils {

    private StringUtils() { }

    public static String uncapitalize(String s) {
        if (!s.isEmpty() && Character.isUpperCase(s.charAt(0)))
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        else
            return s;
    }
}
