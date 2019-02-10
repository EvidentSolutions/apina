plugins {
    id("org.asciidoctor.convert") version "1.5.9.2"
}

tasks.asciidoctor {
    sourceDir = file("src/asciidoc")
    attributes = mapOf("revnumber" to project.version.toString())
}
