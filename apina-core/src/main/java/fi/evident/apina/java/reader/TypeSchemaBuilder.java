package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaTypeVariable;
import fi.evident.apina.java.model.type.TypeSchema;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Support for building a collection of type variables.
 *
 * Since self bounds require variable to be created before the bound is
 * translated, the type variables are now built immediately when they are
 * encountered in bytecode and bounds are then added to the existing
 * variable.
 */
final class TypeSchemaBuilder {

    @Nullable
    private JavaTypeVariable lastVariable;

    @Nullable
    private List<Supplier<JavaType>> currentBounds;

    private final TypeSchema schema = new TypeSchema();

    public void addTypeParameter(String name) {
        finishFormalTypes();

        JavaTypeVariable var = new JavaTypeVariable(name);
        lastVariable = var;
        schema.add(var);
        currentBounds = new ArrayList<>();
    }

    public void addBoundBuilderForLastTypeParameter(Supplier<JavaType> boundBuilder) {
        if (currentBounds == null) throw new IllegalStateException("no current bounds");

        currentBounds.add(boundBuilder);
    }

    public void finishFormalTypes() {
        if (currentBounds != null) {
            assert lastVariable != null;

            for (Supplier<JavaType> bound : currentBounds)
                schema.addBound(lastVariable, bound.get());

            currentBounds = null;
        }
    }

    public TypeSchema getSchema() {
        return schema;
    }
}
