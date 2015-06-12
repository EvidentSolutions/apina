package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.JavaClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class ClassReaderUtils {

    private ClassReaderUtils() { }

    public static JavaClass loadClass(Class<?> cl) {
        try (InputStream in = openInputStreamForClassBytes(cl)) {
            return ClassMetadataReader.loadMetadata(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream openInputStreamForClassBytes(Class<?> cl) throws IOException {
        String resourceName = cl.getName().replace('.', '/') + ".class";
        InputStream inputStream = cl.getClassLoader().getResourceAsStream(resourceName);
        if (inputStream != null)
            return inputStream;
        else
            throw new FileNotFoundException(resourceName);
    }
}
