plugins {
    alias(libs.plugins.asciidoctor)
    alias(libs.plugins.githubPages)
}

tasks.asciidoctor {
    sourceDirProperty.set(file("src/asciidoc"))
    baseDirFollowsSourceDir()
    attributes = mapOf("revnumber" to project.version.toString())
}

tasks.prepareGhPages {
    dependsOn(tasks.asciidoctor)
}

tasks.check {
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
