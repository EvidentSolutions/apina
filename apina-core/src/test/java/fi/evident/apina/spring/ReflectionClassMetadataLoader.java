package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaModel;

import static fi.evident.apina.java.reader.ClassReaderUtils.loadClass;

final class ReflectionClassMetadataLoader {

    public static JavaModel loadClassesFromInheritanceTree(Class<?> clazz) {
        JavaModel result = new JavaModel();

        for (Class<?> cl = clazz; cl != null && cl != Object.class; cl = cl.getSuperclass())
            loadClassAndInterfacesAt(result, cl);

        return result;
    }

    private static void loadClassAndInterfacesAt(JavaModel result, Class<?> cl) {
        if (!result.containsClass(cl.getName())) {
            result.addClass(loadClass(cl));

            for (Class<?> interfaceClass : cl.getInterfaces())
                loadClassAndInterfacesAt(result, interfaceClass);
        }
    }
}
