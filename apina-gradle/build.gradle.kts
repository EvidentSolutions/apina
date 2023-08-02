plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.gradle.plugin-publish")
    `java-gradle-plugin`
}

val shadowImplementation by configurations.creating
configurations["compileOnly"].extendsFrom(shadowImplementation)
configurations["testImplementation"].extendsFrom(shadowImplementation)

dependencies {
    shadowImplementation(project(":apina-core", "shadow"))
    compileOnly(gradleApi())
    compileOnly(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
