package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.JavaClass;
import fi.evident.apina.java.model.JavaModel;
import fi.evident.apina.java.model.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public final class JavaModelLoader {

    private JavaModelLoader() { }

    private static final Logger log = LoggerFactory.getLogger(JavaModelLoader.class);

    public static JavaModel load(Classpath classpath) throws IOException {
        JavaModel classes = new JavaModel();

        Set<JavaType> duplicates = new LinkedHashSet<>();

        ClassPathScanner.processAllClasses(classpath, in -> {
            JavaClass aClass = ClassMetadataReader.loadMetadata(in);
            if (!classes.containsClass(aClass.getName())) {
                classes.addClass(aClass);
            } else {
                duplicates.add(aClass.getType());
            }
        });

        if (!duplicates.isEmpty()) {
            log.warn("There were {} classes with multiple definitions in classpath. Ignoring duplicate definitions.", duplicates.size());
            log.debug("Classes with multiple definitions: {}", duplicates);
        }

        return classes;
    }
}
