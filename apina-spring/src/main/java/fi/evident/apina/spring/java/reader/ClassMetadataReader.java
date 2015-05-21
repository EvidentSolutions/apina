package fi.evident.apina.spring.java.reader;

import fi.evident.apina.spring.java.model.*;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static fi.evident.apina.spring.java.reader.TypeParser.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads class metadata.
 */
final class ClassMetadataReader {

    public static JavaClass loadMetadata(InputStream in) {

        try {
            MyClassVisitor visitor = new MyClassVisitor();

            ClassReader classReader = new ClassReader(in);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            return visitor.getJavaClass();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JavaVisibility parseVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0)
            return JavaVisibility.PUBLIC;
        else if ((access & Opcodes.ACC_PROTECTED) != 0)
            return JavaVisibility.PROTECTED;
        else if ((access & Opcodes.ACC_PRIVATE) != 0)
            return JavaVisibility.PRIVATE;
        else return JavaVisibility.PACKAGE;
    }

    private static final class MyClassVisitor extends ClassVisitor {

        private JavaClass javaClass;

        public MyClassVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (javaClass != null)
                throw new IllegalStateException("classMetadata already initialized");

            // TODO: use access
            this.javaClass = new JavaClass(
                    parseObjectType(name),
                    parseObjectType(superName),
                    Stream.of(interfaces).map(TypeParser::parseObjectType).collect(toList()));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            JavaAnnotation annotation = new JavaAnnotation(parseTypeDescriptor(desc));
            getJavaClass().addAnnotation(annotation);

            return new MyAnnotationVisitor(annotation);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            JavaField field = new JavaField(name, parseVisibility(access), parseJavaType(desc, signature), access);
            getJavaClass().addField(field);
            return new MyFieldVisitor(field);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodSignature methodSignature = parseMethodSignature(desc, signature);

            JavaMethod method = new JavaMethod(name, parseVisibility(access), methodSignature.getReturnType(), methodSignature.getArgumentTypes(), access);
            getJavaClass().addMethod(method);
            return new MyMethodVisitor(method);
        }

        public JavaClass getJavaClass() {
            if (javaClass == null)
                throw new IllegalStateException("no ClassMetadata available");

            return javaClass;
        }
    }

    private static final class MyAnnotationVisitor extends AnnotationVisitor {

        private final JavaAnnotation annotation;

        public MyAnnotationVisitor(JavaAnnotation annotation) {
            super(Opcodes.ASM5);

            this.annotation = requireNonNull(annotation);
        }

        @Override
        public void visit(String name, Object value) {
            annotation.setAttribute(name, value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            QualifiedName enumType = parseTypeDescriptor(desc);

            annotation.setAttribute(name, new EnumValue(enumType, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            JavaAnnotation nested = new JavaAnnotation(parseTypeDescriptor(desc));
            annotation.setAttribute(name, nested);
            return new MyAnnotationVisitor(nested);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new ArrayAnnotationVisitor(annotation, name);
        }
    }

    private static final class ArrayAnnotationVisitor extends AnnotationVisitor {
        private final JavaAnnotation annotation;
        private final String name;
        private final List<Object> values = new ArrayList<>();

        public ArrayAnnotationVisitor(JavaAnnotation annotation, String name) {
            super(Opcodes.ASM5);
            this.name = requireNonNull(name);
            this.annotation = requireNonNull(annotation);
        }

        @Override
        public void visitEnd() {
            annotation.setAttribute(name, values.toArray());
        }

        @Override
        public void visit(String name, Object value) {
            values.add(value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            QualifiedName enumType = parseTypeDescriptor(desc);

            values.add(new EnumValue(enumType, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            JavaAnnotation nested = new JavaAnnotation(parseTypeDescriptor(desc));
            values.add(nested);
            return new MyAnnotationVisitor(nested);
        }
    }

    private static final class MyFieldVisitor extends FieldVisitor {

        private final JavaField field;

        public MyFieldVisitor(JavaField field) {
            super(Opcodes.ASM5);
            this.field = field;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            JavaAnnotation annotation = new JavaAnnotation(parseTypeDescriptor(desc));
            field.addAnnotation(annotation);
            return new MyAnnotationVisitor(annotation);
        }
    }

    private static final class MyMethodVisitor extends MethodVisitor {

        private final JavaMethod method;

        public MyMethodVisitor(JavaMethod method) {
            super(Opcodes.ASM5);
            this.method = method;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            JavaAnnotation annotation = new JavaAnnotation(parseTypeDescriptor(desc));
            method.addAnnotation(annotation);
            return new MyAnnotationVisitor(annotation);
        }
    }
}
