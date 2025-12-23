plugins {
    alias(libs.plugins.asciidoctor)
}

tasks.asciidoctor {
    sourceDirProperty.set(file("src/asciidoc"))
    baseDirFollowsSourceDir()
    attributes = mapOf("revnumber" to project.version.toString())
}

tasks.check {
    dependsOn(tasks.asciidoctor)
}
