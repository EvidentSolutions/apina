plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "1.2.4"
}

dependencies {
    compile(project(":apina-core"))
    compile("ch.qos.logback:logback-classic:1.1.3")
}

application {
    mainClassName = "fi.evident.apina.cli.Apina"
}

tasks["assemble"].dependsOn("shadowJar")
