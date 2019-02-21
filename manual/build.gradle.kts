plugins {
    id("org.asciidoctor.convert") version "1.5.9.2"
    id("org.ajoberstar.github-pages") version "1.6.0"
}

tasks.asciidoctor {
    sourceDir = file("src/asciidoc")
    attributes = mapOf("revnumber" to project.version.toString())
}

tasks.publishGhPages {
    dependsOn(tasks.asciidoctor)
}

githubPages {
    setRepoUri("git@github.com:EvidentSolutions/apina.git")
    deleteExistingFiles = true

    pages(closureOf<CopySpec> {
        from(files("src/CNAME"))
        from(files("build/asciidoc/html5"))
    })
}
