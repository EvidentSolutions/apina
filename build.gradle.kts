import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

configure(allprojects) {
    group = "fi.evident.apina"
    version = project.findProperty("projectVersion") as String? ?: "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JVM_1_8
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.register("publish") {
    group = "publishing"
    description = "Publishes all artifacts"

    dependsOn(tasks.findByPath(":apina-gradle:publishPlugins"))
    dependsOn(tasks.findByPath(":apina-core:publish"))
    dependsOn(tasks.findByPath(":apina-cli:publish"))
}
