import com.gradle.publish.PluginBundleExtension

plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "0.9.7"
    id("java-gradle-plugin")
}

dependencies {
    compile(project(":apina-core"))

    testCompile("junit:junit")
    testCompile(kotlin("test"))
}

configure<PluginBundleExtension> {
    website = "https://github.com/EvidentSolutions/apina"
    vcsUrl = "https://github.com/EvidentSolutions/apina"
    description = "Gradle plugin for creating TypeScript client code from Spring controllers and Jackson classes"
    tags = listOf("typescript", "angular", "jackson", "spring")

    this.plugins {
        "apinaPlugin" {
            id = "fi.evident.apina"
            displayName = "Gradle Apina plugin"
        }
    }
}
