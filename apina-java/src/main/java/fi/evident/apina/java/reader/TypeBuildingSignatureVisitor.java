package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.JavaBasicType;
import fi.evident.apina.java.model.type.JavaParameterizedType;
import fi.evident.apina.java.model.type.JavaType;
import fi.evident.apina.java.model.type.JavaWildcardType;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * {@link SignatureVisitor} implementation that visits the type hierarchy to build generic types.
 */
final class TypeBuildingSignatureVisitor extends SignatureVisitor implements Supplier<JavaType> {

    @Nullable
    private JavaBasicType name;

    private final List<Supplier<JavaType>> arguments = new ArrayList<>();

    public TypeBuildingSignatureVisitor() {
        super(Opcodes.ASM5);
    }

    private void initName(JavaBasicType name) {
        if (this.name != null)
            throw new IllegalStateException("tried to initialize name twice");

        this.name = name;
    }

    @Override
    public JavaType get() {
        if (name == null)
            throw new IllegalStateException("no name defined for type builder");

        if (arguments.isEmpty())
            return name;
        else
            return new JavaParameterizedType(name, arguments.stream().map(Supplier::get).collect(toList()));
    }

    @Override
    public void visitBaseType(char descriptor) {
        initName(TypeParser.parseTypeDescriptor(String.valueOf(descriptor)));
    }

    @Override
    public void visitTypeVariable(String name) {
        // TODO implement parsing type variables
        throw new UnsupportedOperationException("visitTypeVariable " + name);
    }

    @Override
    public SignatureVisitor visitArrayType() {
        // TODO implement parsing array types
        throw new UnsupportedOperationException("visitArrayType");
    }

    @Override
    public void visitClassType(String name) {
        initName(TypeParser.parseObjectType(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        // TODO implement parsing inner class types
        throw new UnsupportedOperationException("visitInnerClassType " + name);
    }

    @Override
    public void visitTypeArgument() {
        arguments.add(JavaWildcardType::unbounded);
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        TypeBuildingSignatureVisitor nestedVisitor = new TypeBuildingSignatureVisitor();
        arguments.add(typeBuilderForWildcard(wildcard, nestedVisitor));
        return nestedVisitor;
    }

    private static Supplier<JavaType> typeBuilderForWildcard(char wildcard, Supplier<JavaType> typeBuilder) {
        switch (wildcard) {
            case '=':
                return typeBuilder;
            case '+':
                return () -> JavaWildcardType.extending(typeBuilder.get());
            case '-':
                return () -> JavaWildcardType.withSuper(typeBuilder.get());
            default:
                throw new IllegalArgumentException("unknown wildcard: " + wildcard);
        }
    }
}
