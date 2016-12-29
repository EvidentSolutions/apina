package fi.evident.apina.java.reader

import fi.evident.apina.java.model.type.JavaType
import fi.evident.apina.java.model.type.TypeSchema
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.util.Arrays.asList
import java.util.Collections.singletonMap

/**
 * Hamcrest matches for [JavaType]s.
 */
internal object JavaTypeMatchers {

    @JvmStatic
    fun basicType(cl: Class<*>): Matcher<JavaType> {
        return basicType(JavaType.Basic(cl))
    }

    @JvmStatic
    fun basicType(type: JavaType.Basic): Matcher<JavaType> {
        return object : JavaTypeMatcher() {

            override fun matchBasicType(item: JavaType.Basic): Boolean {
                return item == type
            }

            override fun describeTo(description: Description) {
                description.appendValue(type)
            }
        }
    }

    @JvmStatic
    fun arrayType(elementTypeMatcher: Matcher<JavaType>): Matcher<JavaType> {
        return object : JavaTypeMatcher() {

            override fun matchArrayType(item: JavaType.Array): Boolean {
                return elementTypeMatcher.matches(item.elementType)
            }

            override fun describeTo(description: Description) {
                description.appendDescriptionOf(elementTypeMatcher).appendText("[]")
            }
        }
    }

    @JvmStatic
    fun typeVariable(name: String): Matcher<JavaType> {
        return object : JavaTypeMatcher() {

            override fun matchTypeVariable(item: JavaType.Variable): Boolean {
                return name == item.name
            }

            override fun describeTo(description: Description) {
                description.appendText("type variable ").appendValue(name)
            }
        }
    }

    @SafeVarargs
    @JvmStatic
    fun genericType(base: Class<*>, vararg args: Matcher<JavaType>): Matcher<JavaType> {
        return genericType(basicType(base), asList(*args))
    }

    fun genericType(baseMatcher: Matcher<JavaType>, argMatchers: List<Matcher<JavaType>>): Matcher<JavaType> {
        return object : JavaTypeMatcher() {

            override fun matchParameterizedType(item: JavaType.Parameterized): Boolean {
                return baseMatcher.matches(item.baseType) && matchList(item.arguments, argMatchers)
            }

            override fun describeTo(description: Description) {
                description.appendDescriptionOf(baseMatcher).appendList("<", ",", ">", argMatchers)
            }
        }
    }

    fun typeWithRepresentation(typeRepresentation: String): Matcher<in JavaType> {
        return object : TypeSafeMatcher<JavaType>() {
            override fun matchesSafely(item: JavaType): Boolean {
                return typeRepresentation == item.toString()
            }

            override fun describeTo(description: Description) {
                description.appendText("type represented by ").appendValue(typeRepresentation)
            }
        }
    }

    @SafeVarargs
    @JvmStatic
    fun singletonSchema(`var`: String, vararg types: Matcher<JavaType>): Matcher<TypeSchema> {
        return schema(singletonMap(JavaType.Variable(`var`), asList(*types)))
    }

    fun schema(map: Map<JavaType.Variable, List<Matcher<JavaType>>>): Matcher<TypeSchema> {
        return object : TypeSafeMatcher<TypeSchema>() {
            override fun matchesSafely(item: TypeSchema): Boolean {
                val variables = item.variables

                if (variables.size != map.size || !map.keys.containsAll(variables))
                    return false

                for ((key, value) in map)
                    if (!matchList(item.getTypeBounds(key), value))
                        return false

                return true
            }

            override fun describeTo(description: Description) {
                description.appendValue(map)
            }
        }
    }

    private fun matchList(items: List<JavaType>, matchers: List<Matcher<JavaType>>): Boolean {
        if (matchers.size != items.size)
            return false

        var i = 0
        val size = items.size
        while (i < size) {
            if (!matchers[i].matches(items[i]))
                return false
            i++
        }

        return true
    }

    private abstract class JavaTypeMatcher : BaseMatcher<JavaType>() {

        override fun matches(item: Any) = when (item) {
            is JavaType.Array -> matchArrayType(item)
            is JavaType.Basic -> matchBasicType(item)
            is JavaType.Variable -> matchTypeVariable(item)
            is JavaType.Parameterized -> matchParameterizedType(item)
            else -> false
        }

        protected open fun matchBasicType(item: JavaType.Basic): Boolean = false
        protected open fun matchTypeVariable(item: JavaType.Variable): Boolean = false
        protected open fun matchArrayType(item: JavaType.Array): Boolean = false
        protected open fun matchParameterizedType(item: JavaType.Parameterized): Boolean = false
    }
}
