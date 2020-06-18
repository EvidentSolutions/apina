import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    kotlin("jvm")
    java
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

val kotlinVersion: String by rootProject.extra

dependencies {
    // We have to define explicit version here or invalid POM is generated
    compile(kotlin("stdlib", kotlinVersion))
    compile("org.slf4j:slf4j-api:1.7.12")
    compile("org.ow2.asm:asm:8.0.1")

    testImplementation(kotlin("test"))
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.8.6")
    testImplementation("org.springframework:spring-web:4.3.5.RELEASE")
    testImplementation("org.springframework.data:spring-data-commons:2.2.6.RELEASE")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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

artifacts.add("archives", sourcesJar)
artifacts.add("archives", javadocJar)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}

if (hasProperty("bintrayUser")) {
    bintray {
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
            setLabels("typescript", "tsd", "angular", "jackson", "spring")
        })
    }
}
