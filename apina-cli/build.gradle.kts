import java.net.URI

plugins {
    application
    kotlin("jvm")
    `maven-publish`
    signing
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":apina-core", "shadow"))
    implementation(libs.asm)
    implementation(libs.logback)
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("fi.evident.apina.cli.Apina")
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

tasks.shadowJar.configure {
    archiveClassifier.set("")
}

tasks.startScripts.configure {
    dependsOn(tasks.shadowJar)
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
