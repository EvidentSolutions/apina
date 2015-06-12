package fi.evident.apina.model;

import fi.evident.apina.model.type.ApiType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;

import static org.hamcrest.Matchers.containsInAnyOrder;

public final class ModelMatchers {

    private ModelMatchers() {
    }

    public static Matcher<PropertyDefinition> property(String name, ApiType type) {
        return new TypeSafeMatcher<PropertyDefinition>() {
            @Override
            protected boolean matchesSafely(PropertyDefinition item) {
                return name.equals(item.getName()) && type.equals(item.getType());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("PropertyDefinition{name=")
                        .appendValue(name)
                        .appendText(", type=")
                        .appendValue(type)
                        .appendText("}");
            }
        };
    }

    @SafeVarargs
    public static Matcher<? super Collection<PropertyDefinition>> hasProperties(Matcher<PropertyDefinition>... matchers) {
        return containsInAnyOrder(matchers);
    }
}
