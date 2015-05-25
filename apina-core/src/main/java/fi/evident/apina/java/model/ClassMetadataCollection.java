package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;

import java.util.*;

import static fi.evident.apina.utils.CollectionUtils.filter;

/**
 * Contains metadata for all loaded classes.
 */
public final class ClassMetadataCollection {

    private final Map<JavaType,JavaClass> classes = new LinkedHashMap<>();

    public ClassMetadataCollection(Collection<JavaClass> classes) {
        for (JavaClass aClass : classes)
            this.classes.put(aClass.getType(), aClass);
    }

    public Optional<JavaClass> findClass(JavaType type) {
        return Optional.ofNullable(classes.get(type));
    }

    public List<JavaClass> findClassesWithAnnotation(JavaBasicType annotationType) {
        return filter(classes.values(), c -> c.hasAnnotation(annotationType));
    }

    public boolean isInstanceOf(JavaType type, Class<?> requiredType) {
        JavaClass javaClass = classes.get(type);

        if (javaClass != null) {
            return javaClass.getName().equals(requiredType.getName())
                || isInstanceOf(javaClass.getSuperClass(), requiredType)
                || javaClass.getInterfaces().stream().anyMatch(interfaceType -> isInstanceOf(interfaceType, requiredType));

        } else if (type instanceof JavaBasicType) {
            try {
                Class<?> aClass = Class.forName(((JavaBasicType) type).getName());
                return requiredType.isAssignableFrom(aClass);

            } catch (ClassNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
