package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.JavaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * {@link SignatureVisitor} that builds method signature for generic methods.
 */
final class MethodSignatureVisitor extends SignatureVisitor implements Supplier<MethodSignature> {

    @Nullable
    private Supplier<JavaType> returnTypeBuilder;

    private final List<Supplier<JavaType>> parameterTypeBuilders = new ArrayList<>();

    private final TypeSchemaBuilder typeSchemaBuilder = new TypeSchemaBuilder();

    public MethodSignatureVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        typeSchemaBuilder.addTypeParameter(name);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return visitBound();
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return visitBound();
    }

    @NotNull
    private SignatureVisitor visitBound() {
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor();
        typeSchemaBuilder.addBoundBuilderForLastTypeParameter(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        typeSchemaBuilder.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor();
        parameterTypeBuilders.add(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        typeSchemaBuilder.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor();
        returnTypeBuilder = visitor;
        return visitor;
    }

    @Override
    public MethodSignature get() {
        if (returnTypeBuilder == null)
            throw new IllegalStateException("returnTypeBuilder not initialized");

        return new MethodSignature(
                returnTypeBuilder.get(),
                parameterTypeBuilders.stream().map(Supplier::get).collect(toList()),
                typeSchemaBuilder.getSchema());
    }

}
