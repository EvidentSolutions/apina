import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.tag
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig

plugins {
    id("pl.allegro.tech.build.axion-release") version "1.4.1"
    kotlin("jvm") version "1.1.3" apply false
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
        jcenter()
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            when (requested.name) {
                "slf4j-api" -> useVersion("1.7.12")
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

task("publish") {
    group = "publishing"
    description = "Publishes all artifacts"

    dependsOn(tasks.findByPath(":apina-gradle:publishPlugins"))
    dependsOn(tasks.findByPath(":apina-core:bintrayUpload"))
}
