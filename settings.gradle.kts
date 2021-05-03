rootProject.name = "apina"

include(":apina-core")
include(":apina-cli")
include(":apina-gradle")
include(":manual")

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "6.0.0"
        id("org.asciidoctor.jvm.convert") version "3.1.0"
        id("org.ajoberstar.github-pages") version "1.6.0"
        id("com.gradle.plugin-publish") version "0.14.0"
    }
}
