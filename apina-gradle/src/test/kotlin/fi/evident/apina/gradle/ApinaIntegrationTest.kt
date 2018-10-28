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
                public ResponseType bar(@RequestBody RequestType foo) {
                    throw new UnsupportedOperationException();
                }

                @GetMapping("/cat")
                public ParameterizedResponseType<Cat, Dog> cat(@RequestBody ParameterizedRequestType<Elephant> foo) {
                    throw new UnsupportedOperationException();
                }

                public static class RequestType {
                    public MyEnum myEnum;
                }

                public static class ResponseType {
                    public NestedType nestedType;
                }

                public static class ParameterizedRequestType<T> {
                    public MyEnum myEnum;
                }

                public static class ParameterizedResponseType<T, S> {
                    public NestedType nestedType;
                }

                public static class NestedType {
                    public int x;
                }

                public static class Cat {
                    public String name;
                }

                public static class Dog {
                }

                public static class Elephant {
                    public boolean big;
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

        assertTrue("export class ParameterizedRequestType {" in output, "output has ParameterizedRequestType")
        assertTrue("export class ParameterizedResponseType {" in output, "output has ParameterizedResponseType")
        assertTrue("export class Cat {" in output, "output has Cat")
        assertTrue("export class Dog {" in output, "output has Dog")
        assertTrue("export class Elephant {" in output, "output has Elephant")

        assertTrue("config.registerClassSerializer('RequestType', {" in output, "output has serializer for RequestType")
        assertTrue("config.registerClassSerializer('ResponseType', {" in output, "output has serializer for ResponseType")
        assertTrue("config.registerClassSerializer('NestedType', {" in output, "output has serializer for Nested")
        assertTrue("config.registerIdentitySerializer('MyEnum')" in output, "MyEnum has identity serializer")

        assertTrue("config.registerClassSerializer('Cat', {" in output, "output has serializer for Cat")
        assertTrue("config.registerClassSerializer('Dog', {" in output, "output has serializer for Dog")
        assertTrue("config.registerClassSerializer('Elephant', {" in output, "output has serializer for Elephant")


    }
}
