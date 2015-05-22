package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.MethodSignature;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaTypeVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * {@link SignatureVisitor} that builds method signature for generic methods.
 */
final class MethodSignatureVisitor extends SignatureVisitor implements Supplier<MethodSignature> {

    @Nullable
    private Supplier<JavaType> returnTypeBuilder;

    private final List<Supplier<JavaType>> parameterTypeBuilders = new ArrayList<>();

    private final TypeVariableCollection typeVariableCollection;

    public MethodSignatureVisitor(Map<String, JavaTypeVariable> typeVariableMap) {
        super(Opcodes.ASM5);

        typeVariableCollection = new TypeVariableCollection(typeVariableMap);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        typeVariableCollection.addTypeParameter(name);
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
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor(typeVariableCollection.getTypeVariableMap());
        typeVariableCollection.addBoundBuilderForLastTypeParameter(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        typeVariableCollection.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor(typeVariableCollection.getTypeVariableMap());
        parameterTypeBuilders.add(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        typeVariableCollection.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor(typeVariableCollection.getTypeVariableMap());
        returnTypeBuilder = visitor;
        return visitor;
    }

    @Override
    public MethodSignature get() {
        if (returnTypeBuilder == null)
            throw new IllegalStateException("returnTypeBuilder not initialized");

        return new MethodSignature(
                returnTypeBuilder.get(),
                parameterTypeBuilders.stream().map(Supplier::get).collect(toList()));
    }

}
