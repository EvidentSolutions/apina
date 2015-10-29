package fi.evident.apina.java.model.type;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class TypeSchema {

    private final List<JavaTypeVariable> variables = new ArrayList<>();
    private final Map<JavaTypeVariable, List<JavaType>> boundMap = new HashMap<>();

    public void add(JavaTypeVariable var) {
        List<JavaType> old = boundMap.putIfAbsent(var, new ArrayList<>());
        if (old != null)
            throw new IllegalArgumentException("tried to add duplicate variable: " + var);
        variables.add(var);
    }

    public void addBound(JavaTypeVariable var, JavaType bound) {
        getBoundList(var).add(bound);
    }

    public List<JavaType> getTypeBounds(JavaTypeVariable var) {
        return unmodifiableList(getBoundList(var));
    }

    public List<JavaTypeVariable> getVariables() {
        return unmodifiableList(variables);
    }

    @NotNull
    private List<JavaType> getBoundList(JavaTypeVariable var) {
        List<JavaType> bounds = boundMap.get(var);
        if (bounds != null)
            return bounds;
        else
            return emptyList();
    }

    @Override
    public String toString() {
        return boundMap.toString();
    }

    public boolean isEmpty() {
        return boundMap.isEmpty();
    }

    public TypeSchema mergeWithParent(TypeSchema parent) {
        TypeSchema mergedSchema = new TypeSchema();

        // First create a new set of variables, avoiding duplicates
        mergedSchema.variables.addAll(parent.variables);
        for (JavaTypeVariable variable : variables)
            if (!mergedSchema.variables.contains(variable))
                mergedSchema.variables.add(variable);

        // Next create a new boundMap, letting child override mappings defined in parent
        mergedSchema.boundMap.putAll(parent.boundMap);
        mergedSchema.boundMap.putAll(boundMap);

        return mergedSchema;
    }
}
