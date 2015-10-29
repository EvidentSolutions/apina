package fi.evident.apina.java.model.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TypeEnvironment {

    private final Map<JavaTypeVariable, JavaType> env = new HashMap<>();

    private TypeEnvironment() {
    }

    public TypeEnvironment(TypeSchema parentSchema, TypeSchema childSchema) {
        addBindingsFromSchema(parentSchema);
        addBindingsFromSchema(childSchema);
    }

    private void addBindingsFromSchema(TypeSchema schema) {
        for (JavaTypeVariable var : schema.getVariables())
            env.put(var, effectiveBounds(schema.getTypeBounds(var)));
    }

    public static TypeEnvironment empty() {
        return new TypeEnvironment();
    }

    public Optional<JavaType> lookup(JavaTypeVariable type) {
        return Optional.ofNullable(env.get(type));
    }

    private static JavaType effectiveBounds(List<JavaType> bounds) {
        // TODO: merge the bounds instead of picking the first one
        if (!bounds.isEmpty())
            return bounds.get(0);
        else
            return new JavaBasicType(Object.class);
    }
}
