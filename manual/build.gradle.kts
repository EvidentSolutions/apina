plugins {
    id("org.asciidoctor.jvm.convert")
    id("org.ajoberstar.github-pages")
}

tasks.asciidoctor {
    sourceDirProperty.set(file("src/asciidoc"))
    baseDirFollowsSourceDir()
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
        from(files("build/docs/asciidoc"))
    })
}
