package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaTypeVariable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Support for building a collection of type variables.
 *
 * Since self bounds require variable to be created before the bound is
 * translated, the type variables are now built immediately when they are
 * encountered in bytecode and bounds are then added to the existing
 * variable.
 */
final class TypeVariableCollection {

    @Nullable
    private List<Supplier<JavaType>> currentBounds;

    private final List<JavaTypeVariable> typeVariables = new ArrayList<>();

    private final Map<String, JavaTypeVariable> typeVariableMap = new HashMap<>();

    public TypeVariableCollection() {
    }

    public TypeVariableCollection(Map<String, JavaTypeVariable> typeVariableMap) {
        this.typeVariableMap.putAll(typeVariableMap);
    }

    public void addTypeParameter(String name) {
        finishFormalTypes();

        JavaTypeVariable var = new JavaTypeVariable(name);
        typeVariables.add(var);
        typeVariableMap.put(var.getName(), var);
        currentBounds = new ArrayList<>();
    }

    public void addBoundBuilderForLastTypeParameter(Supplier<JavaType> boundBuilder) {
        if (currentBounds == null) throw new IllegalStateException("no current bounds");

        currentBounds.add(boundBuilder);
    }

    public List<JavaTypeVariable> getTypeVariables() {
        return typeVariables;
    }

    public Map<String, JavaTypeVariable> getTypeVariableMap() {
        return typeVariableMap;
    }

    public boolean isEmpty() {
        return getTypeVariables().isEmpty();
    }

    public void finishFormalTypes() {
        if (currentBounds != null) {
            JavaTypeVariable var = typeVariables.get(typeVariables.size() - 1);

            for (Supplier<JavaType> bound : currentBounds)
                var.addBound(bound.get());

            currentBounds = null;
        }
    }
}
