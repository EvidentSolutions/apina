import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import java.net.URI

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    signing
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

val sourcesJar = task<Jar>("sourcesJar") {
    dependsOn("classes")
    archiveClassifier.set("sources")

    from(sourceSets.main.get().allSource)
}

val javadoc: Javadoc by tasks

val javadocJar = task<Jar>("javadocJar") {
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

tasks.jar {
    enabled = false
}

artifacts.add(configurations.archives.name, tasks.shadowJar)
artifacts.add(configurations.archives.name, sourcesJar)
artifacts.add(configurations.archives.name, javadocJar)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            project.shadow.component(this)
            artifact(sourcesJar)
            artifact(javadocJar)

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
    }

    if (hasProperty("sonatypeUsername")) {
        repositories {
            maven {
                name = "sonatype"

                url = if (version.toString().endsWith("-SNAPSHOT"))
                    URI("https://oss.sonatype.org/content/repositories/snapshots/")
                else
                    URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = property("sonatypeUsername") as String
                    password = property("sonatypePassword") as String
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
