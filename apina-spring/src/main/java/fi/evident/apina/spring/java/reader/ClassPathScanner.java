package fi.evident.apina.spring.java.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import static java.nio.file.Files.isRegularFile;

/**
 * Reads class metadata for classes in classpath.
 */
final class ClassPathScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassPathScanner.class);

    /**
     * Executes given processor for all classes
     */
    public static void processAllClasses(Classpath classpath, ClassPathResourceProcessor processor) throws IOException {
        for (Path classpathEntry : classpath.getRoots())
            processAllClasses(classpathEntry, processor);
    }

    private static void processAllClasses(Path classpathEntry, ClassPathResourceProcessor processor) throws IOException {
        log.debug("Scanning for classes in {}", classpathEntry);

        if (Files.isDirectory(classpathEntry)) {
            forEachClassUnderDirectory(classpathEntry, processor);

        } else if (isRegularFile(classpathEntry) && classpathEntry.toString().endsWith(".jar")) {
            forEachClassInArchive(classpathEntry, processor);

        } else {
            throw new IllegalArgumentException("invalid classpath entry: " + classpathEntry);
        }
    }

    private static void forEachClassInArchive(Path archivePath, ClassPathResourceProcessor processor) throws IOException {
        try (JarInputStream jar = new JarInputStream(Files.newInputStream(archivePath))) {
            while (true) {
                ZipEntry entry = jar.getNextEntry();
                if (entry == null)
                    break;

                if (entry.getName().endsWith(".class"))
                    processor.process(jar);
            }
        }
    }

    private static void forEachClassUnderDirectory(Path directory, ClassPathResourceProcessor processor) throws IOException {
        try {
            Files.walk(directory)
                    .filter(p -> isRegularFile(p) && p.toString().endsWith(".class"))
                    .forEach(file -> {
                        try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
                            processor.process(in);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
