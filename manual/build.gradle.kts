plugins {
    id("org.asciidoctor.convert")
    id("org.ajoberstar.github-pages")
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
