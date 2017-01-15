package fi.evident.apina.utils

import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.Charset

fun readResourceAsString(path: String, charset: Charset = Charsets.UTF_8): String =
        openResourceAsStream(path).reader(charset).use { it.readText() }

fun openResourceAsStream(path: String): InputStream =
        object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: throw FileNotFoundException("could not find classpath resource: $path")
