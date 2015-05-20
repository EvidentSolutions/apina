package fi.evident.apina.spring.java.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Reads class metadata for classes in classpath.
 */
final class ClassPathScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassPathScanner.class);

    public static List<URI> classFiles(Collection<Path> classpath) throws IOException {
        try {
            List<URI> result = classpath.stream()
                    .flatMap(ClassPathScanner::classFiles)
                    .collect(toList());

            log.debug("found {} classes from classpath {}", result.size(), classpath);

            return result;

        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static Stream<URI> classFiles(Path classpathEntry) {
        if (Files.isDirectory(classpathEntry)) {
            return findClassFiles(classpathEntry);

        } else if (Files.isRegularFile(classpathEntry) && classpathEntry.toString().endsWith(".jar")) {
            // TODO: return URIs for all
            throw new UnsupportedOperationException();
        } else {
            throw new IllegalArgumentException("invalid classpath entry: " + classpathEntry);
        }
    }

    private static Stream<URI> findClassFiles(Path directory) {
        try {
            return Files.walk(directory)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".class"))
                    .map(Path::toUri);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
