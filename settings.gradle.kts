rootProject.name = "apina"

include(":apina-core")
include(":apina-cli")
include(":apina-gradle")
include(":manual")

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "6.0.0"
        id("com.jfrog.bintray") version "1.8.4"
        id("org.asciidoctor.convert") version "1.5.9.2"
        id("org.ajoberstar.github-pages") version "1.6.0"
        id("com.gradle.plugin-publish") version "0.12.0"
    }
}
