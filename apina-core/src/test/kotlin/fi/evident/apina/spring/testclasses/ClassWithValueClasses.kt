package fi.evident.apina.spring.testclasses

@JvmInline
value class ValueString(private val string: String)

@JvmInline
value class ValueInteger(private val integer: Int)

class ClassWithValueClasses {
    var valueString = ValueString("test")
    var valueInteger = ValueInteger(2)

    private val privateValueString = ValueString("test")

    fun getPrivateValue() = privateValueString

}
