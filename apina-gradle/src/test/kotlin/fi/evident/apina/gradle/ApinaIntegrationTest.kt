package fi.evident.apina.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApinaIntegrationTest {

    @Test
    fun smokeTest(@TempDir testProjectDir: File) {
        val buildFile = File(testProjectDir, "build.gradle")

        buildFile.writeText("""
            plugins {
                id 'java'
                id 'fi.evident.apina'
            }

            repositories {
                jcenter()
            }

            apina {
                target = file('apina-output.ts')
                imports = ['./apina-types': ['Foo', 'Bar']]
                enumMode = "default"
            }

            dependencies {
                compile 'org.springframework:spring-webmvc:4.3.5.RELEASE'
            }

            """.trimIndent())

        val folder = File(testProjectDir, "src/main/java/apinatest")
        folder.mkdirs()
        File(folder, "ApinaTestController.java").writeText("""
            package apinatest;

            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            @RequestMapping(path = "/foo")
            public class ApinaTestController {

                @GetMapping("/bar")
                public ResponseType bar(@RequestBody RequestType foo) {
                    throw new UnsupportedOperationException();
                }

                public static class RequestType {
                    public MyEnum myEnum;
                }

                public static class ResponseType {
                    public NestedType nestedType;
                }

                public static class NestedType {
                    public int x;
                }

                public enum MyEnum { FOO, BAR }
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("apina")
                .withPluginClasspath()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":apina")?.outcome)

        val output = File(testProjectDir, "apina-output.ts").readText()

        assertTrue("import { Bar, Foo } from './apina-types';" in output, "output has imports")

        assertTrue("export interface RequestType {" in output, "output has RequestType")
        assertTrue("export interface ResponseType {" in output, "output has ResponseType")
        assertTrue("export interface NestedType {" in output, "output has NestedType")
        assertTrue("export enum MyEnum { FOO = \"FOO\", BAR = \"BAR\" }" in output, "output has MyEnum")

        assertTrue("config.registerClassSerializer('RequestType', {" in output, "output has serializer for RequestType")
        assertTrue("config.registerClassSerializer('ResponseType', {" in output, "output has serializer for ResponseType")
        assertTrue("config.registerClassSerializer('NestedType', {" in output, "output has serializer for Nested")
        assertTrue("config.registerIdentitySerializer('MyEnum')" in output, "MyEnum has identity serializer")
    }
}
