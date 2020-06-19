plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish")
    `java-gradle-plugin`
}

val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(project(":apina-core", "shadow"))
    implementation(kotlin("stdlib", kotlinVersion))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

gradlePlugin {
    plugins {
        create("apinaPlugin") {
            id = "fi.evident.apina"
            implementationClass = "fi.evident.apina.gradle.ApinaPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/EvidentSolutions/apina"
    vcsUrl = "https://github.com/EvidentSolutions/apina"
    description = "Gradle plugin for creating TypeScript client code from Spring controllers and Jackson classes"
    tags = listOf("typescript", "angular", "jackson", "spring")

    (plugins) {
        "apinaPlugin" {
            displayName = "Gradle Apina plugin"
        }
    }
}
