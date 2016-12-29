package fi.evident.apina.java.model

enum class JavaVisibility {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    override fun toString() = name.toLowerCase()
}
