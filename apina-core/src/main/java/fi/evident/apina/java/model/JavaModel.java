package fi.evident.apina.java.model;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaType;

import java.util.*;

import static fi.evident.apina.utils.CollectionUtils.filter;

/**
 * Contains metadata for all loaded classes.
 */
public final class JavaModel {

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

    public Optional<JavaClass> findClass(JavaBasicType type) {
        return findClass(type.getName());
    }

    public List<JavaClass> findClassesWithAnnotation(JavaBasicType annotationType) {
        return filter(classes.values(), c -> c.hasAnnotation(annotationType));
    }

    /**
     * Return types for annotations implied by given annotation using same logic as Spring does.
     * E.g. if annotation {@code @Foo} is itself annotated by {@code @Bar}, then annotating an
     * element with {@code @Foo} has the same effect as annotation by {@code @Bar}.
     */
    public Set<JavaBasicType> findAnnotationsImpliedBy(JavaBasicType annotationType) {
        LinkedHashSet<JavaBasicType> result = new LinkedHashSet<>();

        findAnnotationsImpliedBy(annotationType, result);

        return result;
    }

    private void findAnnotationsImpliedBy(JavaBasicType annotationType, Set<JavaBasicType> result) {
        if (result.add(annotationType)) {

            for (JavaClass cl : classes.values())
                if (cl.isAnnotation() && cl.hasAnnotation(annotationType))
                    findAnnotationsImpliedBy(cl.getType().toBasicType(), result);
        }
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
