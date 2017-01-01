package fi.evident.apina.java.reader

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class Classpath {

    private val _roots = mutableListOf<Path>()

    val roots: List<Path>
        get() = _roots

    fun addRoot(root: Path) {
        _roots.add(root)
    }

    override fun toString() = _roots.joinToString(File.pathSeparator)

    companion object {

        fun parse(path: String): Classpath {
            val result = Classpath()

            for (part in path.split(Pattern.quote(File.pathSeparator).toRegex()))
                result.addRoot(Paths.get(part))

            return result
        }
    }
}
