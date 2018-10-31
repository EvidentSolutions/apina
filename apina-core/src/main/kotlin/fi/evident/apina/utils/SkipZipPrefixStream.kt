package fi.evident.apina.utils

import java.io.InputStream

/**
 * An [InputStream] which skips everything in the beginning of stream before 'PK'.
 *
 * Some jars (e.g. runnable jars built by Spring Boot) contain extra stuff before the actual
 * zip contents start. By wrapping the underlying [InputStream] into this, we are able to
 * consume those without trouble.
 */
class SkipZipPrefixStream(stream: InputStream) : InputStream() {

    private val stream = stream.buffered()

    private var skippedPrefix = false

    override fun read(): Int {
        if (!skippedPrefix) {
            skippedPrefix = true
            val buffer = ByteArray(2)

            while (true) {
                stream.mark(2)

                val count = stream.read(buffer)
                if (count == -1)
                    return -1

                stream.reset()
                if (count == 2 && buffer[0] == 'P'.toByte() && buffer[1] == 'K'.toByte())
                    break
                else
                    stream.skip(1)
            }
        }

        return stream.read()
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int =
        if (!skippedPrefix)
            super.read(b, off, len) // take the slow path until prefix has been skipped
        else
            stream.read(b, off, len)

    override fun close() {
        stream.close()
    }
}
