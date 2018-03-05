import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.kotlin.dsl.extra

plugins {
    kotlin("jvm")
    java
    id("maven-publish")
    id("com.jfrog.bintray") version "1.7.3"
}

val kotlinVersion: String by rootProject.extra

dependencies {
    // We have to define explicit version here or invalid POM is generated
    compile(kotlin("stdlib", kotlinVersion))
    compile("org.slf4j:slf4j-api:1.7.12")
    compile("org.ow2.asm:asm:6.0")

    testCompile("junit:junit")
    testCompile(kotlin("test"))
    testCompile("com.fasterxml.jackson.core:jackson-annotations:2.8.6")
    testCompile("org.springframework:spring-web:4.3.5.RELEASE")
}

val sourcesJar = task<Jar>("sourcesJar") {
    dependsOn("classes")
    classifier = "sources"

    from(java.sourceSets.getByName("main").allSource)
}

val javadoc: Javadoc by tasks

val javadocJar = task<Jar>("javadocJar") {
    dependsOn(javadoc)
    classifier = "javadoc"
    from(javadoc.destinationDir)
}

artifacts.add("archives", sourcesJar)
artifacts.add("archives", javadocJar)

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}

if (hasProperty("bintrayUser")) {
    configure<BintrayExtension> {
        user = property("bintrayUser") as String
        key = property("bintrayApiKey") as String

        setPublications("mavenJava")

        publish = true

        pkg(closureOf<BintrayExtension.PackageConfig> {
            repo = "gradle-plugins"
            name = "apina-core"
            userOrg = "evident"

            setLicenses("MIT")
            websiteUrl = "https://github.com/EvidentSolutions/apina"
            vcsUrl = "https://github.com/EvidentSolutions/apina.git"
            desc = "Tool for generating TypeScript client code from Spring controllers and Jackson classes"
            setLabels("typescript", "tsd", "angularjs", "jackson", "spring")
        })
    }
}
