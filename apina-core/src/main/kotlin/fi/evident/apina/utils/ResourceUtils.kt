package fi.evident.apina.utils

import java.io.*
import java.nio.charset.Charset

object ResourceUtils {

    @Throws(IOException::class)
    @JvmStatic
    fun readResourceAsString(path: String, charset: Charset): String {
        openResourceAsReader(path, charset).use { reader ->
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

    @Throws(FileNotFoundException::class)
    fun openResourceAsReader(path: String, charset: Charset): Reader {
        return InputStreamReader(openResourceAsStream(path), charset)
    }

    @Throws(FileNotFoundException::class)
    fun openResourceAsStream(path: String): InputStream {
        val classLoader = ResourceUtils::class.java.classLoader
        val `in` = classLoader.getResourceAsStream(path)
        if (`in` != null)
            return `in`
        else
            throw FileNotFoundException("could not find classpath resource: " + path)
    }
}
