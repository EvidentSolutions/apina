package fi.evident.apina.output.ts

enum class ResultFunctor(private val functorType: String) {
    PROMISE("Promise"),
    OBSERVABLE("Observable");

    fun apply(type: String) = "$functorType<$type>"
}
