plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(project(":apina-core", "shadow"))
    implementation("ch.qos.logback:logback-classic:1.1.3")
    implementation(kotlin("stdlib", kotlinVersion))
}

application {
    mainClassName = "fi.evident.apina.cli.Apina"
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    classifier = "all"
}
