plugins {
    kotlin("jvm")
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

val kotlinVersion: String by rootProject.extra

dependencies {
    // We have to define explicit version here or invalid POM is generated
    shadow(kotlin("stdlib", kotlinVersion))
    shadow("org.slf4j:slf4j-api:1.7.12")
    implementation("org.ow2.asm:asm:8.0.1")

    testImplementation(kotlin("test"))
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.8.6")
    testImplementation("org.springframework:spring-web:4.3.5.RELEASE")
    testImplementation("org.springframework.data:spring-data-commons:2.2.6.RELEASE")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
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
        }
    }
}
