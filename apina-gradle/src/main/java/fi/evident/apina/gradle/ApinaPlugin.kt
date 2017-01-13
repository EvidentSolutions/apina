package fi.evident.apina.gradle

import fi.evident.apina.gradle.tasks.ApinaTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME
import org.gradle.api.plugins.BasePlugin.BUILD_GROUP
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME

open class ApinaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val mainSourceSet = javaConvention.sourceSets.getByName(MAIN_SOURCE_SET_NAME)

        val apina = project.tasks.create(ApinaTask.GENERATE_API_CLIENT_TASK_NAME, ApinaTask::class.java)
        apina.description = "Generates TypeScript client code from Spring controllers and Jackson classes"
        apina.group = BUILD_GROUP

        // TODO: resolve the build directory from project. however we can't simply say project.buildDir
        // here since it's not yet overridden when plugin is applied
        apina.target = project.file("build/apina/apina.ts")
        apina.classpath = mainSourceSet.output + mainSourceSet.compileClasspath
        project.tasks.findByName(ASSEMBLE_TASK_NAME).dependsOn(ApinaTask.GENERATE_API_CLIENT_TASK_NAME)
    }
}
