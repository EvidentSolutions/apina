package fi.evident.apina.java.model.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.evident.apina.utils.CollectionUtils.map;
import static java.util.Objects.requireNonNull;

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
            bind(var, effectiveBounds(schema.getTypeBounds(var)));
    }

    public void bind(JavaTypeVariable var, JavaType type) {
        env.put(requireNonNull(var), requireNonNull(type));
    }

    public static TypeEnvironment empty() {
        return new TypeEnvironment();
    }

    public JavaType resolve(JavaType type) {
        if (type instanceof JavaTypeVariable) {
            JavaTypeVariable var = (JavaTypeVariable) type;
            return lookup(var).orElseThrow(() -> new RuntimeException("unbound type variable: " + var));
        } else {
            return type;
        }
    }

    public List<JavaType> resolve(List<JavaType> arguments) {
        return map(arguments, this::resolve);
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

    @Override
    public String toString() {
        return env.toString();
    }
}
