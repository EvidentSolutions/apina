import java.net.URI

plugins {
    application
    kotlin("jvm")
    alias(libs.plugins.vanniktech.publish)
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

tasks.shadowJar.configure {
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.startScripts.configure {
    dependsOn(tasks.shadowJar)
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
        artifactId = "apina-cli",
        version = project.version as String
    )

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
