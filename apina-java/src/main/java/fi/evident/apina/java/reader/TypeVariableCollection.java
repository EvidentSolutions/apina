package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaTypeVariable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * Support for building a collection of type variables.
 *
 * When we start processing a method signature, we will first get type variables,
 * followed by their bounds. Since the bounds are available only after the name,
 * we delay the process a bit by collecting the current name and builders for
 * current bounds.
 *
 * Whenever a new item is started with {@link #addTypeParameter(String)} or when
 * we finally ask for results with {@link #getTypeVariables()}, we can finish
 * the current item add add it to mapping of type variables.
 */
final class TypeVariableCollection {

    @Nullable
    private String currentName;

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

        currentName = name;
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
        if (currentName != null) {
            assert currentBounds != null;
            JavaTypeVariable var = new JavaTypeVariable(currentName, currentBounds.stream().map(Supplier::get).collect(toList()));
            typeVariables.add(var);
            typeVariableMap.put(var.getName(), var);
            currentName = null;
            currentBounds = null;
        }
    }
}
