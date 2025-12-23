package fi.evident.apina.gradle

import fi.evident.apina.gradle.tasks.ApinaTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

open class ApinaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)

        project.tasks.register(ApinaTask.GENERATE_API_CLIENT_TASK_NAME, ApinaTask::class.java)
    }
}
