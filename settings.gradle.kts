rootProject.name = "apina"

include(":apina-core")
include(":apina-cli")
include(":apina-gradle")
include(":manual")

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "8.1.0"
        id("org.asciidoctor.jvm.convert") version "3.3.2"
        id("org.ajoberstar.github-pages") version "1.6.0"
        id("com.gradle.plugin-publish") version "1.2.0"
    }
}
