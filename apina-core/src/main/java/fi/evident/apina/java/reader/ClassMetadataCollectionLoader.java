package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.ClassMetadataCollection;

import java.io.IOException;

public final class ClassMetadataCollectionLoader {

    private ClassMetadataCollectionLoader() { }

    public static ClassMetadataCollection load(Classpath classpath) throws IOException {
        ClassMetadataCollection classes = new ClassMetadataCollection();

        ClassPathScanner.processAllClasses(classpath, in -> {
            classes.addClass(ClassMetadataReader.loadMetadata(in));
        });

        return classes;
    }
}
