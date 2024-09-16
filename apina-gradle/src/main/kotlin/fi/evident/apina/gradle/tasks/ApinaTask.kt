@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package fi.evident.apina.gradle.tasks

import fi.evident.apina.ApinaProcessor
import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.model.settings.BrandedPrimitiveType
import fi.evident.apina.model.settings.EnumMode
import fi.evident.apina.model.settings.OptionalTypeMode
import fi.evident.apina.model.settings.Platform
import fi.evident.apina.model.settings.TypeWriteMode
import fi.evident.apina.model.type.ApiType
import fi.evident.apina.model.type.ApiTypeName
import fi.evident.apina.spring.EndpointParameterNameNotDefinedException
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

@CacheableTask
abstract class ApinaTask : DefaultTask() {

    @get:OutputFile
    abstract val target: RegularFileProperty

    @get:CompileClasspath
    @get:InputFiles
    abstract var classpath: FileCollection

    @get:Input
    abstract val blackBoxClasses: ListProperty<String>

    @get:Input
    abstract val endpoints: ListProperty<String>

    @get:Input
    abstract val endpointUrlMethods: ListProperty<String>

    @get:Input
    abstract val imports: MapProperty<String, List<String>>

    @get:Input
    abstract val classNameMapping: MapProperty<String, String>

    @get:Input
    abstract val brandedPrimitiveTypes: MapProperty<String, String>

    @get:Input
    abstract val platform: Property<Platform>

    @get:Input
    abstract val typeWriteMode: Property<TypeWriteMode>

    @get:Input
    abstract val optionalTypeMode: Property<OptionalTypeMode>

    @get:Input
    abstract val enumMode: Property<EnumMode>

    @get:Input
    abstract val removedUrlPrefix: Property<String>

    @get:Input
    abstract val reexportImports: Property<Boolean>

    init {
        description = "Generates TypeScript client code from Spring controllers and Jackson classes"
        group = BasePlugin.BUILD_GROUP

        target.convention(project.layout.buildDirectory.file("apina/apina.ts"))
        platform.convention(Platform.ANGULAR)
        typeWriteMode.convention(TypeWriteMode.INTERFACE)
        optionalTypeMode.convention(OptionalTypeMode.NULL)
        enumMode.convention(EnumMode.DEFAULT)
        removedUrlPrefix.convention("")
        reexportImports.convention(false)

        val javaExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        val mainSourceSet = javaExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        classpath = mainSourceSet.output + mainSourceSet.compileClasspath
    }

    @TaskAction
    fun generateTypeScript() {
        try {
            val myClasspath = Classpath()
            for (file in classpath)
                myClasspath.addRoot(file.toPath())

            val processor = ApinaProcessor(myClasspath)

            processor.settings.enumMode = enumMode.get()
            processor.settings.removedUrlPrefix = removedUrlPrefix.get()
            processor.settings.platform = platform.get()
            processor.settings.typeWriteMode = typeWriteMode.get()
            processor.settings.optionalTypeMode = optionalTypeMode.get()
            processor.settings.reexportImports = reexportImports.get()

            endpoints.get().forEach { processor.settings.addControllerPattern(it) }
            endpointUrlMethods.get().forEach { processor.settings.addEndpointUrlMethodPattern(it) }

            for (pattern in blackBoxClasses.get())
                processor.settings.blackBoxClasses.addPattern(pattern)

            for ((key, value) in imports.get())
                processor.settings.addImport(key, value)

            for ((brandedType, implementationType) in brandedPrimitiveTypes.get())
                processor.settings.brandedPrimitiveTypes += BrandedPrimitiveType(
                    brandedType = ApiTypeName(brandedType),
                    implementationType = ApiType.Primitive.forName(implementationType)
                )

            for ((qualifiedName, translatedName) in classNameMapping.get())
                processor.settings.nameTranslator.registerClassName(qualifiedName, translatedName)

            val output = processor.process()

            target.get().asFile.writeText(output)

        } catch (e: EndpointParameterNameNotDefinedException) {
            logger.error("{}\nConsider adding 'compileJava { options.compilerArgs = ['-parameters'] }' to your build file.", e.message)
            throw e
        }
    }

    companion object {
        const val GENERATE_API_CLIENT_TASK_NAME = "apina"
    }
}
