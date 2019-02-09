import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig

plugins {
    id("pl.allegro.tech.build.axion-release") version "1.4.1"
    kotlin("jvm") version "1.2.40" apply false
}

val kotlinVersion by extra("1.2.40") // same as above :(

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
        jcenter()
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            when (requested.name) {
                "junit" -> useVersion("4.12")
                "hamcrest-core" -> useTarget("${requested.group}:hamcrest-all:${requested.version}")
            }
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
}

tasks.register("publish") {
    group = "publishing"
    description = "Publishes all artifacts"

    dependsOn(tasks.findByPath(":apina-gradle:publishPlugins"))
    dependsOn(tasks.findByPath(":apina-core:bintrayUpload"))
}
