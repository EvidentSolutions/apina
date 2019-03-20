package fi.evident.apina.model.settings

enum class Platform {
    ANGULAR,

    @Deprecated(message = "Use ANGULAR instead", replaceWith = ReplaceWith("Platform.ANGULAR", "fi.evident.apina.model.settings.Platform"))
    ANGULAR2,
    ES6,

    SWIFT
}
