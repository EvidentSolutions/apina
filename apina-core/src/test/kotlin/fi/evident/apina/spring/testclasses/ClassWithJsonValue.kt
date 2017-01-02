package fi.evident.apina.spring.testclasses

import com.fasterxml.jackson.annotation.JsonValue

class ClassWithJsonValue {
    var bar = ""
    var baz = ""

    @JsonValue
    fun foo() = "foo"
}
