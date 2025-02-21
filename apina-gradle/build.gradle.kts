plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginPublish)
    `java-gradle-plugin`
}

val shadowImplementation by configurations.creating

configurations.compileOnly.configure {
    extendsFrom(shadowImplementation)
}

configurations.testImplementation.configure {
    extendsFrom(shadowImplementation)
}

dependencies {
    shadowImplementation(project(":apina-core", "shadow"))
    compileOnly(gradleApi())
    compileOnly(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

gradlePlugin {
    website.set("https://github.com/EvidentSolutions/apina")
    vcsUrl.set("https://github.com/EvidentSolutions/apina")

    plugins {
        create("apinaPlugin") {
            id = "fi.evident.apina"
            displayName = "Gradle Apina plugin"
            implementationClass = "fi.evident.apina.gradle.ApinaPlugin"
            description = "Gradle plugin for creating TypeScript client code from Spring controllers and Jackson (or Kotlin serialization) classes"
            tags.set(listOf("typescript", "angular", "jackson", "spring"))
        }
    }
}

tasks.shadowJar.configure {
    archiveBaseName.set("apina-gradle")
    archiveAppendix.set("")
    archiveClassifier.set("")

    configurations = listOf(shadowImplementation)

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("org.slf4j:slf4j-api"))
    }
}

tasks.jar {
    enabled = false
    dependsOn(tasks.shadowJar)
}
