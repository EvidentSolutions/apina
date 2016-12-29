package fi.evident.apina.java.model.type

import fi.evident.apina.java.model.JavaClass

/**
 * Represents a class bound to a type-environment.
 */
class BoundClass(val javaClass: JavaClass, val environment: TypeEnvironment)
