import java.net.URI

plugins {
    application
    kotlin("jvm")
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow")
}

val asmVersion: String by rootProject.extra

dependencies {
    implementation(project(":apina-core", "shadow"))
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation(kotlin("stdlib"))
}

application {
    mainClassName = "fi.evident.apina.cli.Apina"
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

tasks.shadowJar {
    classifier = ""
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
                name.set("apina-cli")
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
