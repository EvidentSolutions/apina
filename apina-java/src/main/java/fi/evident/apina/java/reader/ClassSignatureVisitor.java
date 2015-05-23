package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaType;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * {@link SignatureVisitor} that resolves generic types for a class:
 * <ul>
 *     <li>generic variables with their bounds if class defines any</li>
 *     <li>generic super-class signature</li>
 *     <li>generic ignatures of implemented interfaces</li>
 * </ul>
 */
final class ClassSignatureVisitor extends SignatureVisitor {

    private final TypeVariableCollection typeVariableCollection = new TypeVariableCollection();

    private Optional<Supplier<JavaType>> superClassBuilder = Optional.empty();

    private final List<Supplier<JavaType>> interfaceBuilders = new ArrayList<>();

    private ClassSignatureVisitor() {
        super(Opcodes.ASM5);
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

    public TypeVariableCollection getTypeVariables() {
        return typeVariableCollection;
    }

    public Optional<JavaType> getSuperClass() {
        return superClassBuilder.map(Supplier::get);
    }

    public List<JavaType> getInterfaces() {
        return interfaceBuilders.stream().map(Supplier::get).collect(toList());
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        typeVariableCollection.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor(typeVariableCollection.getTypeVariableMap());
        superClassBuilder = Optional.of(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitInterface() {
        typeVariableCollection.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor(typeVariableCollection.getTypeVariableMap());
        interfaceBuilders.add(visitor);
        return visitor;
    }

    public static ClassSignatureVisitor parse(String signature) {
        ClassSignatureVisitor visitor = new ClassSignatureVisitor();
        new SignatureReader(signature).accept(visitor);
        visitor.getTypeVariables().finishFormalTypes();
        return visitor;
    }
}
