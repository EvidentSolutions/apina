package fi.evident.apina.spring.testclasses

@JvmInline
value class ValueString(private val string: String)

@JvmInline
value class ValueInteger(private val integer: Int)

@JvmInline
value class ValueNested(private val v: ValueInteger)

class ClassWithValueClasses {
    var valueString = ValueString("test")
    var valueInteger = ValueInteger(2)
    var nestedValueInteger = ValueNested(ValueInteger(2))

    fun getGetterValue() = ValueString("test")

}
