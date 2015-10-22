package fi.evident.apina.spring;

import fi.evident.apina.java.model.ClassMetadataCollection;
import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;

import static fi.evident.apina.java.reader.ClassReaderUtils.loadClass;

final class ReflectionClassMetadataLoader {

    public static ClassMetadataCollection loadClassesFromInheritanceTree(Class<?> clazz) {
        ClassMetadataCollection result = new ClassMetadataCollection();

        for (Class<?> cl = clazz; cl != null && cl != Object.class; cl = cl.getSuperclass())
            loadClassAndInterfacesAt(result, cl);

        return result;
    }

    private static void loadClassAndInterfacesAt(ClassMetadataCollection result, Class<?> cl) {
        JavaType type = new JavaBasicType(cl);

        if (!result.containsClass(type)) {
            result.addClass(loadClass(cl));

            for (Class<?> interfaceClass : cl.getInterfaces())
                loadClassAndInterfacesAt(result, interfaceClass);
        }
    }
}
