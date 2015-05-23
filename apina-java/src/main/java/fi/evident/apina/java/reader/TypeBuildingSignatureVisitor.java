package fi.evident.apina.java.reader;

import fi.evident.apina.java.model.type.*;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * {@link SignatureVisitor} implementation that visits the type hierarchy to build generic types.
 */
final class TypeBuildingSignatureVisitor extends SignatureVisitor implements Supplier<JavaType> {

    @Nullable
    private Function<List<JavaType>, JavaType> builder;

    private final List<Supplier<JavaType>> arguments = new ArrayList<>();

    public TypeBuildingSignatureVisitor() {
        super(Opcodes.ASM5);
    }

    private void initBuilder(Function<List<JavaType>, JavaType> builder) {
        if (this.builder != null)
            throw new IllegalStateException("tried to initialize builder twice");

        this.builder = builder;
    }

    @Override
    public JavaType get() {
        if (builder == null)
            throw new IllegalStateException("no builder defined for visitor");

        List<JavaType> argumentTypes = arguments.stream().map(Supplier::get).collect(toList());

        return builder.apply(argumentTypes);
    }

    @Override
    public void visitBaseType(char descriptor) {
        JavaType baseType = TypeParser.parseTypeDescriptor(String.valueOf(descriptor));
        initBuilder(args -> {
            assert args.isEmpty();
            return baseType;
        });
    }

    @Override
    public void visitTypeVariable(String name) {
        initBuilder(args -> {
            assert args.isEmpty();
            return new JavaTypeVariable(name);
        });
    }

    @Override
    public SignatureVisitor visitArrayType() {
        TypeBuildingSignatureVisitor nestedVisitor = new TypeBuildingSignatureVisitor();

        initBuilder(args -> {
            assert args.isEmpty();
            return new JavaArrayType(nestedVisitor.get());
        });

        return nestedVisitor;
    }

    @Override
    public void visitClassType(String name) {
        JavaType baseType = TypeParser.parseObjectType(name);
        initBuilder(args -> args.isEmpty() ? baseType : new JavaParameterizedType(baseType, args));
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
