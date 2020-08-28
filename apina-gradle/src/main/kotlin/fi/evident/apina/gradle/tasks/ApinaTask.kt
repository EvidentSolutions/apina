@file:Suppress("MemberVisibilityCanBePrivate")

package fi.evident.apina.gradle.tasks

import fi.evident.apina.ApinaProcessor
import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.model.settings.EnumMode
import fi.evident.apina.model.settings.Platform
import fi.evident.apina.model.settings.TypeWriteMode
import fi.evident.apina.spring.EndpointParameterNameNotDefinedException
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.*
import org.gradle.util.GFileUtils.writeFile
import java.io.File
import java.util.*
import kotlin.properties.Delegates

@CacheableTask
open class ApinaTask : DefaultTask() {

    @get:OutputFile
    var target: File by Delegates.notNull()

    @get:CompileClasspath
    @get:InputFiles
    var classpath: FileCollection by Delegates.notNull()

    @get:Input
    var blackBoxClasses: List<String> = ArrayList()

    @get:Input
    var endpoints: List<String> = ArrayList()

    @get:Input
    var endpointUrlMethods: List<String> = ArrayList()

    @get:Input
    var imports: Map<String, List<String>> = HashMap()

    @get:Input
    var platform = Platform.ANGULAR

    @get:Input
    var typeWriteMode = TypeWriteMode.INTERFACE

    @get:Input
    var enumMode = EnumMode.DEFAULT

    @get:Input
    var removedUrlPrefix = ""

    init {
        description = "Generates TypeScript client code from Spring controllers and Jackson classes"
        group = BasePlugin.BUILD_GROUP

        // TODO: resolve the build directory from project. however we can't simply say project.buildDir
        // here since it's not yet overridden when plugin is applied
        target = project.file("build/apina/apina.ts")

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val mainSourceSet = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        classpath = mainSourceSet.output + mainSourceSet.compileClasspath
    }

    @TaskAction
    fun generateTypeScript() {
        try {
            val myClasspath = Classpath()
            for (file in classpath)
                myClasspath.addRoot(file.toPath())

            val processor = ApinaProcessor(myClasspath)

            processor.settings.enumMode = enumMode
            processor.settings.removedUrlPrefix = removedUrlPrefix
            processor.settings.platform = platform
            processor.settings.typeWriteMode = typeWriteMode

            endpoints.forEach { processor.settings.addControllerPattern(it) }
            endpointUrlMethods.forEach { processor.settings.addEndpointUrlMethodPattern(it) }

            for (pattern in blackBoxClasses)
                processor.settings.blackBoxClasses.addPattern(pattern)

            for ((key, value) in imports)
                processor.settings.addImport(key, value)

            val output = processor.process()

            writeFile(output, target, "UTF-8")

        } catch (e: EndpointParameterNameNotDefinedException) {
            logger.error("{}\nConsider adding 'compileJava { options.compilerArgs = ['-parameters'] }' to your build file.", e.message)
            throw e
        }
    }

    companion object {
        const val GENERATE_API_CLIENT_TASK_NAME = "apina"
    }
}
