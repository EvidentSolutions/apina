plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "0.10.0"
    `java-gradle-plugin`
}

dependencies {
    compile(project(":apina-core"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

pluginBundle {
    website = "https://github.com/EvidentSolutions/apina"
    vcsUrl = "https://github.com/EvidentSolutions/apina"
    description = "Gradle plugin for creating TypeScript client code from Spring controllers and Jackson classes"
    tags = listOf("typescript", "angular", "jackson", "spring")
}

gradlePlugin {
    plugins {
        create("apinaPlugin") {
            id = "fi.evident.apina"
            displayName = "Gradle Apina plugin"
            implementationClass = "fi.evident.apina.gradle.ApinaPlugin"
        }
    }
}
