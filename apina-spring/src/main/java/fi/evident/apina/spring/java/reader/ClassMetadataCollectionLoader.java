package fi.evident.apina.spring.java.reader;

import fi.evident.apina.spring.java.model.ClassMetadataCollection;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public final class ClassMetadataCollectionLoader {

    private ClassMetadataCollectionLoader() { }

    public static ClassMetadataCollection load(Collection<Path> classpath) throws IOException {
        List<URI> classResources = ClassPathScanner.classFiles(classpath);
        return ClassMetadataReader.loadMetadataForClasses(classResources);
    }
}
