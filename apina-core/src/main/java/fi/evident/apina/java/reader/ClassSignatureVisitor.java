package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.TypeSchema;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static fi.evident.apina.utils.CollectionUtils.map;

/**
 * {@link SignatureVisitor} that resolves generic types for a class:
 * <ul>
 *     <li>generic variables with their bounds if class defines any</li>
 *     <li>generic super-class signature</li>
 *     <li>generic signatures of implemented interfaces</li>
 * </ul>
 */
final class ClassSignatureVisitor extends SignatureVisitor {

    private final TypeSchemaBuilder typeSchemaBuilder = new TypeSchemaBuilder();

    private Optional<Supplier<JavaType>> superClassBuilder = Optional.empty();

    private final List<Supplier<JavaType>> interfaceBuilders = new ArrayList<>();

    private ClassSignatureVisitor() {
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

    public Optional<JavaType> getSuperClass() {
        return superClassBuilder.map(Supplier::get);
    }

    public List<JavaType> getInterfaces() {
        return map(interfaceBuilders, Supplier::get);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        typeSchemaBuilder.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor();
        superClassBuilder = Optional.of(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitInterface() {
        typeSchemaBuilder.finishFormalTypes();
        TypeBuildingSignatureVisitor visitor = new TypeBuildingSignatureVisitor();
        interfaceBuilders.add(visitor);
        return visitor;
    }

    @Override
    public void visitEnd() {
        typeSchemaBuilder.finishFormalTypes();
    }

    public static ClassSignatureVisitor parse(String signature) {
        ClassSignatureVisitor visitor = new ClassSignatureVisitor();
        new SignatureReader(signature).accept(visitor);
        return visitor;
    }

    public TypeSchema getSchema() {
        return typeSchemaBuilder.getSchema();
    }
}
