package fi.evident.apina.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApinaIntegrationTest {

    @get:Rule
    @Suppress("MemberVisibilityCanBePrivate")
    val testProjectDir = TemporaryFolder()

    private val buildFile: File by lazy { testProjectDir.newFile("build.gradle") }

    @Test
    fun smokeTest() {
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

        val folder = testProjectDir.newFolder("src", "main", "java", "apinatest")
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
                public ResponseType<Cat> bar(@RequestBody RequestType foo) {
                    throw new UnsupportedOperationException();
                }

                public static class RequestType {
                    public MyEnum myEnum;
                }

                public static class ResponseType<T> {
                    public NestedType nestedType;
                }

                public static class NestedType {
                    public int x;
                }

                public static class Cat {
                    public String name;
                }

                public enum MyEnum { FOO, BAR }
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("apina")
                .withPluginClasspath()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":apina")?.outcome)

        val output = File(testProjectDir.root, "apina-output.ts").readText()

        assertTrue("import { Bar, Foo } from './apina-types';" in output, "output has imports")

        assertTrue("export class RequestType {" in output, "output has RequestType")
        assertTrue("export class ResponseType {" in output, "output has ResponseType")
        assertTrue("export class NestedType {" in output, "output has NestedType")
        assertTrue("export enum MyEnum { FOO = \"FOO\", BAR = \"BAR\" }" in output, "output has MyEnum")
        assertTrue("export class Cat {" in output, "output has Cat")

        assertTrue("config.registerClassSerializer('RequestType', {" in output, "output has serializer for RequestType")
        assertTrue("config.registerClassSerializer('ResponseType', {" in output, "output has serializer for ResponseType")
        assertTrue("config.registerClassSerializer('NestedType', {" in output, "output has serializer for Nested")
        assertTrue("config.registerClassSerializer('Cat', {" in output, "output has serializer for Cat")
        assertTrue("config.registerIdentitySerializer('MyEnum')" in output, "MyEnum has identity serializer")
    }
}
