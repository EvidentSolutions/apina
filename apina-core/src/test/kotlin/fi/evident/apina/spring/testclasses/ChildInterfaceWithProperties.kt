package fi.evident.apina.spring.testclasses

interface ChildInterfaceWithProperties : ParentInterfaceWithProperties {

    val bar: String
        get() = "bar"
}
