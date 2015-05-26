package fi.evident.apina.utils;

import static fi.evident.apina.utils.StringUtils.uncapitalize;

public final class PropertyUtils {

    private PropertyUtils() { }

    public static String propertyNameForGetter(String getterName) {
        if (getterName.startsWith("get"))
            return uncapitalize(getterName.substring(3));
        else if (getterName.startsWith("is"))
            return uncapitalize(getterName.substring(2));
        else
            throw new IllegalArgumentException("not a valid name for getter "+ getterName);
    }
}
