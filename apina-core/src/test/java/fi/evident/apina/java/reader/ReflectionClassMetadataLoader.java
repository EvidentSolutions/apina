package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.JavaModel;

import java.lang.annotation.Annotation;

import static fi.evident.apina.java.reader.ClassReaderUtils.loadClass;

public final class ReflectionClassMetadataLoader {

    public static JavaModel loadClassesFromInheritanceTree(Class<?> clazz) {
        JavaModel result = new JavaModel();

        for (Class<?> cl = clazz; cl != null && cl != Object.class; cl = cl.getSuperclass())
            loadClassesAt(result, cl);

        return result;
    }

    private static void loadClassesAt(JavaModel result, Class<?> cl) {
        if (!result.containsClass(cl.getName())) {
            result.addClass(loadClass(cl));

            for (Annotation annotation : cl.getDeclaredAnnotations())
                loadClassesAt(result, annotation.annotationType());

            for (Class<?> interfaceClass : cl.getInterfaces())
                loadClassesAt(result, interfaceClass);
        }
    }
}
