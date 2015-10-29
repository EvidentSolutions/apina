package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.evident.apina.utils.CollectionUtils.filter;

/**
 * Contains metadata for all loaded classes.
 */
public final class ClassMetadataCollection {

    private final Map<String,JavaClass> classes = new LinkedHashMap<>();

    public void addClass(JavaClass aClass) {
        JavaClass old = classes.putIfAbsent(aClass.getName(), aClass);
        if (old != null)
            throw new IllegalStateException("Class " + aClass.getName() + " was already added");
    }

    public boolean containsClass(String name) {
        return classes.containsKey(name);
    }

    public Optional<JavaClass> findClass(String name) {
        return Optional.ofNullable(classes.get(name));
    }

    public List<JavaClass> findClassesWithAnnotation(JavaBasicType annotationType) {
        return filter(classes.values(), c -> c.hasAnnotation(annotationType));
    }

    public boolean isInstanceOf(JavaType type, Class<?> requiredType) {
        JavaClass javaClass = classes.get(type.getNonGenericClassName());

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

    public boolean isNumber(JavaBasicType type) {
        return type.isPrimitiveNumber() || isInstanceOf(type, Number.class);
    }

    @Override
    public String toString() {
        return classes.toString();
    }
}
