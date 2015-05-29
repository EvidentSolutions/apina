package fi.evident.apina.gradle;

import fi.evident.apina.gradle.tasks.ApinaTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import static fi.evident.apina.gradle.tasks.ApinaTask.GENERATE_API_CLIENT_TASK_NAME;
import static org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.api.plugins.BasePlugin.BUILD_GROUP;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

public class ApinaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(MAIN_SOURCE_SET_NAME);

        ApinaTask apina = project.getTasks().create(GENERATE_API_CLIENT_TASK_NAME, ApinaTask.class);
        apina.setDescription("Generates TypeScript client code from Spring controllers and Jackson classes");
        apina.setGroup(BUILD_GROUP);
        apina.setTarget(project.file("build/apina/apina.ts"));
        apina.setClasspath(mainSourceSet.getOutput().plus(mainSourceSet.getCompileClasspath()));
        project.getTasks().findByName(ASSEMBLE_TASK_NAME).dependsOn(GENERATE_API_CLIENT_TASK_NAME);
    }
}
