package fi.evident.apina.cli

import fi.evident.apina.ApinaProcessor
import fi.evident.apina.java.reader.Classpath
import fi.evident.apina.spring.EndpointParameterNameNotDefinedException
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Apina {

    private val log = LoggerFactory.getLogger(Apina::class.java)

    @JvmStatic fun main(args: Array<String>) {
        val arguments = CommandLineArguments.parse(args)

        if (arguments.files.size != 1 && arguments.files.size != 2) {
            System.err.printf("usage: apina INPUT1%sINPUT2%s... [OUTPUT]\n", File.pathSeparator, File.pathSeparator)
            exitProcess(1)
        }

        try {
            val classpath = Classpath.parse(arguments.files[0])

            val processor = ApinaProcessor(classpath)

            processor.settings.platform = arguments.platform
            processor.settings.typeWriteMode = arguments.typeWriteMode
            processor.settings.optionalTypeMode = arguments.optionalTypeMode

            for (blackBoxPattern in arguments.blackBoxPatterns)
                processor.settings.blackBoxClasses.addPattern(blackBoxPattern)

            for (pattern in arguments.controllerPatterns)
                processor.settings.addControllerPattern(pattern)

            for (pattern in arguments.endpointUrlMethods)
                processor.settings.addEndpointUrlMethodPattern(pattern)

            for (anImport in arguments.imports)
                processor.settings.addImport(anImport.module, anImport.types)

            processor.settings.brandedPrimitiveTypes += arguments.brandedPrimitiveTypes

            val output = processor.process()

            if (arguments.files.size == 2) {
                val outputFile = Paths.get(arguments.files[1])

                log.debug("Writing API to '{}'", outputFile)
                Files.createDirectories(outputFile.parent)
                Files.write(outputFile, output.toByteArray())
            } else {
                println(output)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            exitProcess(1)

        } catch (e: EndpointParameterNameNotDefinedException) {
            System.err.println(e.message)
            exitProcess(2)
        }
    }
}
