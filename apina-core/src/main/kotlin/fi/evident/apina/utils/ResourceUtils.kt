package fi.evident.apina.utils

import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.Charset

fun readResourceAsString(path: String, charset: Charset): String {
    openResourceAsStream(path).reader(charset).use { reader ->
        val sb = StringBuilder()
        val buffer = CharArray(1024)

        while (true) {
            val n = reader.read(buffer)
            if (n == -1)
                break
            sb.append(buffer, 0, n)
        }

        return sb.toString()
    }
}

fun openResourceAsStream(path: String): InputStream =
        object {}.javaClass.classLoader.getResourceAsStream(path)
                ?: throw FileNotFoundException("could not find classpath resource: $path")

