package fi.evident.apina.model

import fi.evident.apina.model.type.ApiType
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.TypeSafeMatcher

object ModelMatchers {

    @JvmStatic
    fun property(name: String, type: ApiType): Matcher<PropertyDefinition> {
        return object : TypeSafeMatcher<PropertyDefinition>() {
            override fun matchesSafely(item: PropertyDefinition): Boolean {
                return name == item.name && type == item.type
            }

            override fun describeTo(description: Description) {
                description.appendText("PropertyDefinition{name=")
                        .appendValue(name)
                        .appendText(", type=")
                        .appendValue(type)
                        .appendText("}")
            }
        }
    }

    @JvmStatic
    @SafeVarargs
    fun hasProperties(vararg matchers: Matcher<PropertyDefinition>): Matcher<in Collection<PropertyDefinition>> {
        return containsInAnyOrder(*matchers)
    }
}
