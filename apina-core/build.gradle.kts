import java.net.URI

plugins {
    kotlin("jvm")
    java
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow")
}

repositories {
    mavenCentral()
}

val asmVersion: String by rootProject.extra
val kotlinMetadataVersion = "0.3.0"

dependencies {
    // We have to define explicit version here or invalid POM is generated
    shadow(kotlin("stdlib"))
    shadow("org.slf4j:slf4j-api:1.7.32")
    shadow("org.jetbrains.kotlinx:kotlinx-metadata-jvm:$kotlinMetadataVersion")
    implementation("org.ow2.asm:asm:$asmVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:$kotlinMetadataVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.12.5")
    testImplementation("org.springframework:spring-web:5.3.9")
    testImplementation("org.springframework.data:spring-data-commons:2.5.4")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.2.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
