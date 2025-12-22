import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import java.net.URI

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    shadow(kotlin("stdlib"))
    shadow(libs.slf4j)
    shadow(libs.kotlin.metadata)
    implementation(libs.asm)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.metadata)
    testImplementation(libs.jackson.annotations)
    testImplementation(libs.spring.web)
    testImplementation(libs.spring.data.commons)
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlinx.serialization.core)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.compileTestJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    })
}

tasks.compileTestKotlin {
    compilerOptions {
        jvmTarget = JVM_17
    }
}

tasks.test {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    })
}

tasks.shadowJar {
    archiveBaseName.set("apina-core")
    archiveAppendix.set("")
    archiveClassifier.set("")
    relocate("org.objectweb.asm", "fi.evident.apina.libs.org.objectweb.asm")
    dependencies {
        include(dependency("org.ow2.asm:asm"))
    }
}

tasks.jar {
    archiveClassifier.set("plain")
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "fi.evident.apina",
        artifactId = "apina-core",
        version = project.version as String
    )

    pom {
        name.set("apina-core")
        description.set("Tool for creating TypeScript client code from Spring controllers and Jackson classes")
        url.set("https://github.com/evidentsolutions/apina")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("komu")
                name.set("Juha Komulainen")
            }
        }

        scm {
            connection.set("scm:git:git@github.com:evidentsolutions/apina.git")
            developerConnection.set("scm:git:git@github.com:evidentsolutions/apina.git")
            url.set("https://github.com/evidentsolutions/apina")
        }
    }
}
