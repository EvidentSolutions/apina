package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Hamcrest matches for {@link JavaType}s.
 */
final class JavaTypeMatchers {

    public static Matcher<JavaType> basicType(Class<?> cl) {
        return basicType(new JavaBasicType(cl));
    }

    public static Matcher<JavaType> basicType(JavaBasicType type) {
        return new JavaTypeMatcher() {

            @Override
            protected boolean matchBasicType(JavaBasicType item) {
                return item.equals(type);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(type);
            }
        };
    }

    public static Matcher<JavaType> arrayType(Matcher<JavaType> elementTypeMatcher) {
        return new JavaTypeMatcher() {

            @Override
            protected boolean matchArrayType(JavaArrayType item) {
                return elementTypeMatcher.matches(item.getElementType());
            }

            @Override
            public void describeTo(Description description) {
                description.appendDescriptionOf(elementTypeMatcher).appendText("[]");
            }
        };
    }

    @SafeVarargs
    public static Matcher<JavaType> typeVariable(String name, Matcher<JavaType>... boundMatchers) {
        return typeVariable(name, asList(boundMatchers));
    }

    private static Matcher<JavaType> typeVariable(String name, List<Matcher<JavaType>> boundMatchers) {
        return new JavaTypeMatcher() {

            @Override
            protected boolean matchTypeVariable(JavaTypeVariable item) {
                return name.equals(item.getName())
                        && matchList(item.getBounds(), boundMatchers);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("type variable ").appendValue(name).appendText(" with " + boundMatchers.size() + " bounds");
            }
        };
    }

    @SafeVarargs
    public static Matcher<JavaType> genericType(Class<?> base, Matcher<JavaType>... args) {
        return genericType(basicType(base), asList(args));
    }

    public static Matcher<JavaType> genericType(Matcher<JavaType> baseMatcher, List<Matcher<JavaType>> argMatchers) {
        return new JavaTypeMatcher() {

            @Override
            protected boolean matchParameterizedType(JavaParameterizedType item) {
                return baseMatcher.matches(item.getBaseType())
                        && matchList(item.getArguments(), argMatchers);
            }

            @Override
            public void describeTo(Description description) {
                description.appendDescriptionOf(baseMatcher).appendList("<", ",", ">", argMatchers);
            }
        };
    }

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

    private static boolean matchList(List<JavaType> items, List<Matcher<JavaType>> matchers) {
        if (matchers.size() != items.size())
            return false;

        for (int i = 0, size = items.size(); i < size; i++)
            if (!matchers.get(i).matches(items.get(i)))
                return false;

        return true;
    }

    private static abstract class JavaTypeMatcher extends BaseMatcher<JavaType> {

        @Override
        public boolean matches(Object item) {
            if (item instanceof JavaArrayType)
                return matchArrayType((JavaArrayType) item);
            else if (item instanceof JavaBasicType)
                return matchBasicType((JavaBasicType) item);
            else if (item instanceof JavaTypeVariable)
                return matchTypeVariable((JavaTypeVariable) item);
            else if (item instanceof JavaParameterizedType)
                return matchParameterizedType((JavaParameterizedType) item);
            else
                return false;
        }

        protected boolean matchBasicType(JavaBasicType item) {
            return false;
        }

        protected boolean matchTypeVariable(JavaTypeVariable item) {
            return false;
        }

        protected boolean matchArrayType(JavaArrayType item) {
            return false;
        }

        protected boolean matchParameterizedType(JavaParameterizedType item) {
            return false;
        }
    }
}
