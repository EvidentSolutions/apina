package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matches for {@link JavaType}s.
 */
final class JavaTypeMatchers {

    public static Matcher<? super JavaType> typeWithRepresentation(String typeRepresentation) {
        return new TypeSafeMatcher<JavaType>() {
            @Override
            protected boolean matchesSafely(JavaType item) {
                return typeRepresentation.equals(item.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("type represented by ").appendValue(typeRepresentation);
            }
        };
    }
}
