import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig

plugins {
    alias(libs.plugins.axion.release)
    alias(libs.plugins.kotlin.jvm) apply false
}

configure<VersionConfig> {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
}

configure(allprojects) {
    group = "fi.evident.apina"
    this.version = rootProject.scmVersion.version

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
    dependsOn(tasks.findByPath(":manual:publishGhPages"))
}
